use loyaltyDB;

// Clear existing data
db.transactions.drop();

// Insert transactions from Transactions.csv
db.transactions.insertMany([
    {
        transactionId: "t1",
        customerId: "c1",
        amount: 100,
        date: ISODate("2025-04-29T10:00:00Z"),
        items: ["item1", "item2"],
        couponUsed: "COUPON10"
    },
    {
        transactionId: "t2",
        customerId: "c2",
        amount: 150,
        date: ISODate("2025-04-30T12:00:00Z"),
        items: ["item3"],
        couponUsed: null
    },
    {
        transactionId: "t3",
        customerId: "c1",
        amount: 200,
        date: ISODate("2025-05-01T14:00:00Z"),
        items: ["item4"],
        couponUsed: null
    },
    {
        transactionId: "t4",
        customerId: "c4",
        amount: 80,
        date: ISODate("2025-05-05T09:00:00Z"),
        items: ["item5"],
        couponUsed: "COUPON20"
    },
    {
        transactionId: "t5",
        customerId: "c5",
        amount: 120,
        date: ISODate("2025-05-10T11:00:00Z"),
        items: ["item6", "item7"],
        couponUsed: null
    },
    {
        transactionId: "t6",
        customerId: "c2",
        amount: 90,
        date: ISODate("2025-05-15T13:00:00Z"),
        items: ["item8"],
        couponUsed: "COUPON10"
    },
    {
        transactionId: "t7",
        customerId: "c1",
        amount: 130,
        date: ISODate("2025-05-20T15:00:00Z"),
        items: ["item9"],
        couponUsed: null
    },
    {
        transactionId: "t8",
        customerId: "c4",
        amount: 110,
        date: ISODate("2025-05-25T08:00:00Z"),
        items: ["item10"],
        couponUsed: null
    },
    {
        transactionId: "t9",
        customerId: "c5",
        amount: 140,
        date: ISODate("2025-05-27T16:00:00Z"),
        items: ["item11"],
        couponUsed: "COUPON15"
    },
    // Add a late-night transaction for testing
    {
        transactionId: "t10",
        customerId: "c1",
        amount: 175,
        date: ISODate("2025-06-05T23:00:00Z"), // 11 PM UTC, 4 AM PKT on June 6
        items: ["item12", "item13"],
        couponUsed: "COUPON25"
    }
]);