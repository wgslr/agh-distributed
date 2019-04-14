#!/usr/bin/env python3

import pika
import sys
import datetime

from interactive_server import InteractiveServer
import common
from common import errprint


class Admin(InteractiveServer):
    def __init__(self):
        # matching 2-word keys filters out 'info'
        queues = [('*.*', self._log)]
        super().__init__(self._handle_line, queues)

    def _handle_line(self, line):
        self.channel.basic_publish(exchange=common.EXCHANGE,
                                   routing_key='info',
                                   body=line)

    def _log(self, ch, method, properties, body):
        time = datetime.datetime.now()

        print('[{}] key: {}: {}'.format(
            time, method.routing_key, body.decode()))


if __name__ == '__main__':
    admin = Admin()
    admin.start()
