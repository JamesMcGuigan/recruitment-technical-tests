// Project in a bank
// Customers -> Accounts
// Rules:
// - Joint Accounts
// - Belong to a Company
// - Account Number
// - Balance


var data = {
    "accounts": [
        {
            "id":        "acc1",
            "balance":   0,
            "isJoint":   false,
            "owner1id":  "",
            "owner2id":  null
        },
        {
            "id":        "acc2",
            "balance":   1000,
            "isJoint":   false,
            "owner1id":  "",
            "owner2id":  null
        },
        {
            "id":        "acc3",
            "balance":   2000,
            "isJoint":   false,
            "owner1id":  "",
            "owner2id":  null
        }
    ],
    "people": [
        {
            "id":   "",
            "name": "",
            "companyID": ""
        }
    ],
    "companies": [{
        "id":   "",
        "name": ""
    }]
};


/**
 *
 * @param {Object} data      json data
 * @param {Number} balance   minimum balance to search for
 */
var findAccountsWithMinBalance = function(data, balance) {
    _.filter( data["accounts"], function(account) {
        return account.balance >= balance;
    });
};

/**
 * @param {object} data
 */
var extendAccountsWithCompanyNames = function(data) {
    var companyIdToName = {};
    _.each(data.companies, function(company) {
        companyIdToName[company.id] = company.name;
    });
    var personIdToCompanyName = {};
    _.each(data.people, function(person) {
        personIdToCompanyName[person.id] = companyIdToName[person.companyID];
    });

    var accountWithNames = _.map( data["accounts"], function(account) {
        var account = $.extend(true, {},account);
        account["companyName"] = personIdToCompanyName["account.owner1id"];
        return account;
    });
};


var findAccountsWithMinBalanceTest = function() {
    var testData = $.extend(true, {}, data);

    var accounts = findAccountsWithMinBalance(testData, 1000);
    assert(accounts.length == 2);
    assert(_.pluck(accounts, "id")[0], "acc2");
    assert(_.pluck(accounts, "id")[1], "acc3");
};






