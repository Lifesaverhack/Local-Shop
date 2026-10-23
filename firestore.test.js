const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const CHARLIE_UID = "charlie_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- Unauthenticated tests ---
test("Unauthenticated user: cannot create listing", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("listings").doc("item1").set({
    sellerId: ALICE_UID,
    sellerName: "Alice",
    title: "iPhone 13",
    description: "Good condition smartphone",
    priceNpr: 65000,
    category: "Electronics",
    condition: "Used - Good",
    city: "Dhangadhi",
    status: "active",
    createdAt: new Date(),
  }));
});

// --- Listings tests ---
test("Authenticated user: can create and read their listing", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("listings").doc("item1").set({
    sellerId: ALICE_UID,
    sellerName: "Alice Sharma",
    title: "Mountain Bike",
    description: "Barely used mountain bike in Dhangadhi",
    priceNpr: 18000,
    category: "Vehicles",
    condition: "Used - Like New",
    city: "Dhangadhi",
    status: "active",
    createdAt: new Date(),
  }));

  // Another user can read active listing
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("listings").doc("item1").get());
});

test("Cross-user: Bob cannot edit Alice's listing", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("listings").doc("item1").set({
      sellerId: ALICE_UID,
      sellerName: "Alice Sharma",
      title: "Mountain Bike",
      description: "Barely used mountain bike in Dhangadhi",
      priceNpr: 18000,
      category: "Vehicles",
      condition: "Used - Like New",
      city: "Dhangadhi",
      status: "active",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("listings").doc("item1").update({
    title: "Hacked Bike",
  }));
});

test("Owner: Alice can mark her listing as sold", async () => {
  const date = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("listings").doc("item1").set({
      sellerId: ALICE_UID,
      sellerName: "Alice Sharma",
      title: "Mountain Bike",
      description: "Barely used mountain bike in Dhangadhi",
      priceNpr: 18000,
      category: "Vehicles",
      condition: "Used - Like New",
      city: "Dhangadhi",
      status: "active",
      createdAt: date,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("listings").doc("item1").update({
    status: "sold",
  }));
});

// --- Chats tests ---
test("Chat thread: Only participants can access chat", async () => {
  const date = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("chats").doc("chat1").set({
      listingId: "item1",
      buyerId: BOB_UID,
      sellerId: ALICE_UID,
      participantIds: [BOB_UID, ALICE_UID],
      createdAt: date,
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("chats").doc("chat1").get());

  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  await assertFails(charlieDb.collection("chats").doc("chat1").get());
});

// --- Favorites tests ---
test("Favorites: User can save and read own favorites, cannot access others", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("favorites").doc("item1").set({
    listingId: "item1",
    createdAt: new Date(),
  }));

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("favorites").doc("item1").get());
});
