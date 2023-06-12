#======================== Launch start rabbitmq: ========================
# 1. As service (and restart at login):
$ brew services start rabbitmq

# 2. As a shell process:
$ rabbitmq-server


#======================== Runtime monitoring and config: ========================
# Management UI (username/passwd = guest/guest)
# Management Plugin enabled by default at:
http://localhost:15672/


#======================== CLI management: ========================
# 1. rabbitmqctl
$ rabbitmqctl list_users
$ rabbitmqctl change_password guest guest4rabbitmq

# 2. rabbitmq-diagnostics
$ rabbitmq-diagnostics list_queues

# 3. rabbitmq-plugins...
#Activate management plugin
$ rabbitmq-plugins enable rabbitmq_management

#List installed plugins
$ rabbitmq-plugins list
