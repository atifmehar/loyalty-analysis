use loyaltyDB;

// Clear existing data
db.customers.drop();
// Insert customers
db.customers.insertMany([
    {
        _id:"c1",
		name: "Alice Smith",
        email: "alice.smith@example.com",
        joinDate: "2025-04-22",
        status: "active"
    },{
        _id:"c2",
		name: "Bob Johnson",
        email: "bob.johnson@example.com",
        joinDate: "2025-04-23",
        status: "active"
    },{
        _id:"c3",
		name: "John Doe",
        email: "john.doe@example.com",
        joinDate: "2025-04-23",
        status: "Inactive"
    },{
        _id:"c4",
		name: "diana Prince",
        email: "diana.prince@example.com",
        joinDate: "2025-04-23",
        status: "active"
    },{
        _id:"c5",
		name: "Eve Adams",
        email: "eve.adams@example.com",
        joinDate: "2025-04-24",
        status: "active"
    }
]);