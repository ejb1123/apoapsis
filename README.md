apoapsis
========

[![Build Status](https://travis-ci.org/robotbrain/apoapsis.svg?branch=master)](https://travis-ci.org/robotbrain/apoapsis)
[![Download](https://api.bintray.com/packages/robotbrain/maven/apoapsis/images/download.svg) ](https://bintray.com/robotbrain/maven/apoapsis/_latestVersion)

# Sample conversation:
-> token:wrong_token

<- rx:err:badtoken

-> token:right_token

<- rx:token



-> list:servers

<- rx:list:[]

-> create:{"name": "test", "location": "./test", "version": {"name": "1.8.1", "base": "https://s3.amazonaws.com/Minecraft.Download/versions"}}

<- rx:created:00000000-0000-0000-0000-000000000000


-> list:servers

<- rx:list:[{"name": "test", "uuid": "00000000-0000-0000-0000-000000000000", "location": "./test"}]


-> select:00000000-0000-0000-0000-000000000000

<- rx:ok

-> status

<- rx:status:notrunning


-> start

<- status:00000000-0000-0000-0000-000000000000:init

<- out:00000000-0000-0000-0000-000000000000:LINE OF SERVER STDOUT

<- err:00000000-0000-0000-0000-000000000000:LINE OF SERVER STDERR

<- started:00000000-0000-0000-0000-000000000000:100

<- status:00000000-0000-0000-0000-000000000000:running

<- join:00000000-0000-0000-0000-000000000000:Player

<- chat:00000000-0000-0000-0000-000000000000:Player:Hi there

-> list:players

<- rx:list:["Player"]

<- part:00000000-0000-0000-0000-000000000000:Player

-> list:players

<- rx:list:[]


-> say:Hi there

-> cmd:?

<- out:00000000-0000-0000-0000-000000000000:Stuff

-> stop

<- status:00000000-0000-0000-0000-000000000000:deinit

<- out:00000000-0000-0000-0000-000000000000:Stuff

<- stop:00000000-0000-0000-0000-000000000000

<- status:00000000-0000-0000-0000-000000000000:notrunning


-> changeversion:{"name": "1.8", "base": "https://s3.amazonaws.com/Minecraft.Download/versions"}

<- rx:changeversion:1.8


-> delete

<- rx:delete:ok

-> status

<- rx:err:noserverselected
