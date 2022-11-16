package com.example.study

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
// 코루틴 Context에서 코루틴이 실행되는데
// 코루틴 Context 요소에는 Dispatcher가 존재
// Dispatcher는 코루틴이 어떤 스레드, 스레드풀에서 실행될지를 결정함
// 모든 코루틴 builder들은 옵셔널로 coroutineContext parameter를 받는다.
// 명시적으로 Dispatcher를 줄 수도, 안줄 수도 있다.

fun main() = runBlocking<Unit> {
    // 4개의 코루틴을 디버깅 해보는 예제

    // 파라미터로 아무것도 안받는 코루틴은 자신을 호출했던 코루틴scope의 context를 상속받아 수행함.
    // runBlocking 스레드이므로 main에서 실행될 것이다.
    launch {
        println("main runBlocking :"  +
                "I'm working in thread ${Thread.currentThread().name}")
    }
    // Unconfined에서 만든 코루틴이지만 메인스레드에서 실행됨.
    launch(Dispatchers.Unconfined){
        println("Unconfined :"  +
                "I'm working in thread ${Thread.currentThread().name}")
    }
    launch(Dispatchers.Default) {
        println("Default :"  +
                "I'm working in thread ${Thread.currentThread().name}")
    }
    // 스레드를 하나 만드는 방식 -> 비용이 많이 든다.
    // 단 이 방법은 스레드를 close해줘야 하기에 use를 사용한다.
    newSingleThreadContext("MyOwnThread").use {
        launch(it) {
            println("newSingleThreadContext :"  +
                    "I'm working in thread ${Thread.currentThread().name}")
        }
    }
}
**/

/**
// Jumping between threads
// withContext()를 이용하면 코루틴 context를 변경할 수 있다.
fun main() {
    newSingleThreadContext("Ctx1").use { ctx1 ->
        newSingleThreadContext("Ctx2").use { ctx2 ->
            runBlocking(ctx1) {
                // ctx1이라는 스레드에서 실행됨.
                println("Started in ctx1")

                // ctx2라는 스레드에서 실행됨.
                withContext(ctx2) { // withContext에 Dispatcher를 넣어주면 해당 스레드에서 코루틴이 실행됨.
                    println("Working in ctx2")

                }
                // 다시 ctx1으로 돌아옴.
                println("Back to ctx1")

                // 단, 이 동작들은 다 하나의 코루틴에서 실행된다는 것이 중요하다 !
            }
        }
    }
}
**/

/**
// Job in the context
fun main() = runBlocking<Unit> {
    // 실행된 코루틴의 context에서 job이라는 요소를 꺼냄
    // 이 job은 BlockingCoroutine
    // 즉, 코루틴 Context안에는 dispatcher말고도 job도 있다.
    println("My job is ${coroutineContext[Job]}")

    // 이전 예제에서 다뤘었던 isActive는
    // coroutineContext에서 Job을 꺼내서 job이 실행되고 있는지를 확인하는 거
    // 단순 편의성을 제공하기 위한 프로퍼티인 isActive
    coroutineContext[Job]?.isActive ?: true
}
**/

/**
// 새로운 코루틴의 job은 부모 코루틴의 job의 child가 된다.
// 단, GlobalScope를 사용하게 되면 별도로 job이 생성되고 부모 자식 관계가 성립하지 않는다.
// GlobalScope는 앱 전체를 범위로 하기 때문이다.
fun main() = runBlocking<Unit> {
    val request = launch {
        // Global에서 수행됨.
        GlobalScope.launch {
            println("Job1 : I run in GlobalScope and execute independently!")
            delay(1000)
            println("Job1 : End!")
        }

        launch {
            delay(100)
            println("Job2 : I am a child of the request coroutine")
            delay(1000)
            // 부모가 cancel 되므로 아래 println문은 출력되지 않음.
            println("job2 : I will not execute this line if my parent request")
        }
    }
    delay(500)
    request.cancel() // 부모가 cancel 되면서 launch 코루틴(자식코루틴)도 같이 취소됨.
    delay(1000)
}
**/

/**
// Parental responsibilities
// 모든 자식 코루틴들의 수행이 완료될 때까지 기다려줌
// 직접 트래킹할 필요가 없다. 즉, join을 사용하지 않아도 부모 코루틴에서 자식 코루틴을 만들면
// 자식 코루틴의 완료를 다 기다려준다.
fun main() = runBlocking {
    val request = launch { // runBlocking 코루틴은 여기 launch가 끝날 때까지 기다려줄 것이고
        repeat(3) { i ->
            launch { // 위에 launch코루틴은 여기 launch 코루틴이 끝날 때까지 기다려줄 것이다.
                delay( (i+1) * 200L )
                println("Coroutine $i is done")
            }
        }
        println("request: I'm done and I don't explicitly join my children")
    }
    println("Now processing of the request is complete")
}
**/

/**
// Combining context elements
// 코루틴 elements들을 합쳐서 하나로 만들기
fun main() = runBlocking<Unit> {
    launch( // + 연산자는 코루틴을 더하는 기능을 한다.
        Dispatchers.Default + CoroutineName("test")
    ){
        println("I'm working in thread ${Thread.currentThread().name}")
    }
}
**/

/**
// Coroutine scope
// 하나의 Coroutine scope에서 여러 코루틴들을 만들면
// 나중에 이 Coroutine scope를 종료할 때 엮여있는 모든 코루틴들이 같이 종료될 수 있게 만들 수 있다.
class Activity{
    private val mainScope = CoroutineScope(Dispatchers.Default)

    fun destroy() {
        mainScope.cancel()
    }

    fun doSomething() {
        repeat(10) { i -> // 코루틴을 10개 만드는 동작
            mainScope.launch {
                delay( (i+1) * 200L)
                println("Coroutine $i is done")
            }
        }
    }
} // class Activity ends

fun main() = runBlocking<Unit> {
    val activity = Activity()
    activity.doSomething()
    println("Launched coroutines")
    delay(500L)
    println("Destorying activity")
    activity.destroy() // mainscope의 cancel을 일으키면 mainscope에서 생성된 코루틴들이 다 종료된다.
    delay(3000)
}
**/