use loyaltyDB;

// Clear existing data
db.rewards.drop();


// Insert rewards from rewards.csv
db.rewards.insertMany([
    {
        _id: "r1",
        customerId: "c1",
        pointsEarned: 100,
        pointsRedeemed: 0,
        date: ISODate("2025-04-29T10:00:00Z")
    },
    {
        _id: "r2",
        customerId: "c2",
        pointsEarned: 150,
        pointsRedeemed: 0,
        date: ISODate("2025-04-30T12:00:00Z")
    },
    {
        _id: "r3",
        customerId: "c1",
        pointsEarned: 200,
        pointsRedeemed: 150,
        date: ISODate("2025-05-02T10:00:00Z")
    },
    {
        _id: "r4",
        customerId: "c4",
        pointsEarned: 80,
        pointsRedeemed: 0,
        date: ISODate("2025-05-05T09:00:00Z")
    },
    {
        _id: "r5",
        customerId: "c5",
        pointsEarned: 120,
        pointsRedeemed: 0,
        date: ISODate("2025-05-10T11:00:00Z")
    },
    {
        _id: "r6",
        customerId: "c2",
        pointsEarned: 90,
        pointsRedeemed: 50,
        date: ISODate("2025-05-16T13:00:00Z")
    },
    {
        _id: "r7",
        customerId: "c1",
        pointsEarned: 130,
        pointsRedeemed: 0,
        date: ISODate("2025-05-20T15:00:00Z")
    },
    {
        _id: "r8",
        customerId: "c4",
        pointsEarned: 110,
        pointsRedeemed: 80,
        date: ISODate("2025-05-26T08:00:00Z")
    }
]);