#!/usr/bin/env python3

import sys, Ice

Ice.loadSlice('../api/bank.ice')
import Bank

with Ice.initialize(sys.argv) as communicator:
    factory_prx = communicator.stringToProxy('accfac/accountfactory:tcp -h localhost -p 10000:udp -h localhost -p 10000')
    print(factory_prx)
    factory = Bank.AccountFactoryPrx.checkedCast(factory_prx)
    print(factory)
    income = Bank.MoneyAmount(10000, Bank.Currency.PLN)
    result = factory.createAccount("w", "g", "1234", income)
    print(result)
    key = result.key
    account = result.account

    result2 = factory.createAccount("w", "g", "1234", income)
    print(result2)
    key2 = result2.key
    account2 = result2.account


    # proxy = communicator.stringToProxy('standard/somepesel:tcp -h localhost -p 10000:udp -h localhost -p 10000')
    # print('got proxy')
    # account = Bank.AccountPrx.checkedCast(proxy)
    # print('got account')

    print("getting balance")
    balance= account.getBalance({'key': key + "badkey"})
    print(balance)
    print(balance.minorUnitAmount)

