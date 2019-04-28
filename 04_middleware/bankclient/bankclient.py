#!/usr/bin/env python3

import sys, Ice

Ice.loadSlice('../api/bank.ice')
import Bank

with Ice.initialize(sys.argv) as communicator:
    proxy = communicator.stringToProxy('standard/somepesel:tcp -h localhost -p 10001:udp -h localhost -p 10001')
    print('got proxy')
    account = Bank.AccountPrx.checkedCast(proxy)
    print('got account')

    balance= account.getBalance()
    print(balance)
    print('got account')
    print(balance.minorUnitAmount)

