#!/usr/bin/env python3

import sys, Ice

Ice.loadSlice('../api/bank.ice')
import bank

with Ice.initialize(sys.argv) as communicator:
    # TODO make the client interactive  to switch between accounts
    # TODO read server port from commandline

    factory_prx = communicator.stringToProxy('accfac/accountfactory:tcp -h localhost -p 10002:udp -h localhost -p 10002')
    print(factory_prx)
    factory = bank.AccountFactoryPrx.checkedCast(factory_prx)
    print(factory)
    income = bank.MoneyAmount(100020, bank.Currency.PLN)
    result = factory.createAccount("w", "g", "1234", income)
    print(result)
    key = result.key

    if result.isPremium:
        print("Casting to premium account")
        account = bank.PremiumAccountPrx.checkedCast(result.account)

        print("requesting loan")
        
        loanValue = bank.MoneyAmount(3000, bank.Currency.GBP)
        offer = account.requestLoan(loanValue, 15, {'key': key})
        print(offer)
    else:
        print("standard account")
        account = result.account

    result2 = factory.createAccount("wewe", "g", "12345", income)
    print(result2)
    key2 = result2.key
    account2 = result2.account


    # proxy = communicator.stringToProxy('standard/somepesel:tcp -h localhost -p 10002:udp -h localhost -p 10002')
    # print('got proxy')
    # account = Bank.AccountPrx.checkedCast(proxy)
    # print('got account')

    # print("getting balance")
    # balance= account.getBalance({'key': key + "badkey"})
    # print(balance)
    # print(balance.minorUnitAmount)