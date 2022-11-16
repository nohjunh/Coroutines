package com.example.study

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

/**
--------------------------------------------------

https://youtu.be/YrrUCSi72E8
KotlinConf 2017 - Deep Dive into Coroutines on JVM by Roman Elizarov
코루틴이 어떻게 동작하는지에 대한 conf

CPS transformation
-> JVM에 들어갈 때는, 우리가 호출한 함수에 Continuation cont가 인수에 생긴다.

-------------------------------------------------

https://youtu.be/eJF60hcz3EU
Kotlin Coroutines 톺아보기

-------------------------------------------------

https://kotlinworld.com/141
https://velog.io/@haero_kim/Kotlin-Coroutine-Dispatchers-1%ED%8E%B8

Coroutine's Dispatcher
 Dispatch = '보내다'
 스레드(Thread)에 코루틴(Coroutine)을 보낸다.
 코루틴에서는 스레드 풀을 만들고 Dispatcher를 통해서 코루틴을 배분
 ->
 코루틴을 만든 다음 해당 코루틴을 Dispatcher에 전송하면 Dispatcher는
자신이 관리하는 스레드풀 내의 스레드의 부하 상황에 맞춰 코루틴을 배분
 ->
 해당 코루틴이 어떤 스레드 위에서 실행되게 할지 명시
 코루틴의 실행을 특정 스레드에 국한 시켜주거나, 특정 스레드 풀로 전달해주는 역할을 함.


 안드로이드에서는 Dispatcher가 이미 생성되어 있다.
 1. Dispatchers.Main : 안드로이드 메인 스레드에서 코루틴을 실행하는 디스패처
                        UI와 상호작용하는 작업을 실행할 때만 사용한다.
 2. Dispatchers.IO : 디스크 or 네트워크 I/O 작업을 실행하는데 최적화되어 있는 디스패처
 3. Dispatchers.Default : CPU를 많이 사용하는 작업을 기본 스레드 외부에서 실행하도록 최적화되어 있는 디스패처

 ------------------------------------------------



 **/
