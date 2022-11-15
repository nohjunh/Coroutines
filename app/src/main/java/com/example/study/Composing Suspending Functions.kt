package com.example.study

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

/**
// Sequential by default
// 비동기를 하게 되면 순서를 맞추기 어려운데
// 이러한 비동기들을 어떻게 순서대로 처리하는가에 대한 Part
fun main() = runBlocking {
    val time = measureTimeMillis {
        // 코루틴은 아래 비동기 함수들이 순차적으로 실행된다. ( 쌓여가는 구조 )
        // 다른 비동기 프로그램은 아래 두 함수가 동시에 수행되지만
        // 코루틴은 레귤러 함수를 적는 것처럼 알아서 순차적으로 비동기 함수들을 처리하게 된다.
        // 코루틴은 콜백을 순차적으로 처리할 수 있게 해준다.
        // 메인 스레드에서 코루틴을 수행해도 UI를 멈추지 않고 수행할 수 있다는 것도 코루틴의 장점.
        val one = doSomethingUsefulOne() // 비동기로 동작하는 함수(네트워크 콜같은 헤비한 동작이라고 가정)
        val two = doSomethingUsefulTwo() // 비동기로 동작하는 함수
        println("The answer is ${one + two}")
    }
    println("Completed in $time ms")
}
// 코루틴이 일시 중단되는 suspend 함수
suspend fun doSomethingUsefulOne() : Int {
    println("One")
    delay(1000L) // 원래 이 자리에 서버 호출 하는 등 헤비한 작업을 한다. 여기서는 단순히 delay로 처리
    return 13
}

suspend fun doSomethingUsefulTwo() : Int {
    println("Two")
    delay(1000L)
    return 29
}
**/

/**
// Concurrent using async
// 두 suspend 비동기 함수들이 독립적이라면
// 순차적으로 실행하지 않고 각각 수행하여 더 빠르게 결과 값을 얻을 수도 있다. ( 두 함수를 동시에 수행하므로 더 빠르게 가능 )
// 즉, 순차적으로 비동기함수를 수행하는게 아니라 동시에 하고 싶으면 async를 통해 명시적으로 호출하면 된다.
fun main () = runBlocking {
    val time = measureTimeMillis {
        val one = async { doSomethingUsefulOne () } // async가 있으므로 이 함수를 수행하고 바로 다음 코드라인을 수행하기 위해 내려간다.
        val two = async { doSomethingUsefulTwo () }
        // 명시적으로 호출한 await()를 통해 async의 수행이 끝날 때까지 기다리는 함수.
        // one, two의 결과가 다 나온 후에 아래 코드의 결과도 수행된다고 보면 됨.
        println("The answer is ${one.await () + two.await () }")
    }
    println("Completed in $time ms")
}

suspend fun doSomethingUsefulOne() : Int {
    delay(1000L)
    return 13
}

suspend fun doSomethingUsefulTwo() : Int {
    delay(1000L)
    return 29
}
**/

/**
// Lazily started async
// async 코루틴을 나중에 수행할 수 있는 방법
// async 자체는 코루틴 빌더
// async 파라미터로 LAZY를 넣어주면 코루틴이 바로 수행되는 것을 막고
// start()를 통해 코루틴이 실행될 수 있도록 함.

fun main () = runBlocking {
    val time = measureTimeMillis {
        // async는 기본적인 옵션은 CoroutineStart.DEFAULT 그러나, LAZY로 설정할 수도 있음.
        val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }
        val two = async(start = CoroutineStart.LAZY) { doSomethingUsefulTwo() }
        // some computation
        one.start()
        two.start()
        // 만약 위에 start들을 주석처리해 사용하지 않는다면,
        // 아래 one.await()를 만났을 때 doSomethingUsefulOne()이 수행되며 끝날 때까지 대기하고 결과값을 받음
        // 그 다음 two.await()를 만났을 때 위와 같은 동일 과정 수행.
        println("The answer is ${one.await() + two.await()}")
    }
    println("completed in $time ms")
}
suspend fun doSomethingUsefulOne() : Int {
    delay(1000L)
    return 13
}

suspend fun doSomethingUsefulTwo() : Int {
    delay(1000L)
    return 29
}
**/

/**
// Structured concurrency with async
// exception이 발생하면 모든 코루틴이 취소될 것이다.
// 일반함수처럼 코루틴 함수를 사용하려면
// coroutineScope로 감싸서 사용해야 안전하다는 뜻.
fun main () = runBlocking {
    val time = measureTimeMillis {
        // 다음과 같이 concurrentSum 함수를 다른 곳에서도 활용할 수 있도록
        // 코드를 짰을 때 안전하게 모든 코루틴이 수행되도록 하려면
        // suspend fun concurrentSum() : Int = coroutineScope { 처럼
        // coroutineScope으로 감싸줘야 된다는 뜻.
        println("The answer is ${ concurrentSum() }")
    }
    println("Completed in $time ms")
}

suspend fun concurrentSum() : Int = coroutineScope {
    val one = async { doSomethingUsefulOne() }
    val two = async { doSomethingUsefulTwo() }
    one.await() + two.await()
}

suspend fun doSomethingUsefulOne() : Int {
    delay(1000L)
    return 13
}

suspend fun doSomethingUsefulTwo() : Int {
    delay(1000L)
    return 29
}
**/

/**
// Cancellation propagated coroutines hierarchy
// 만약 async로 수행되는 코루틴들이 있을 떄 한 코루틴에서 exception이 일어나면
// exception이 propagated 되면서 연쇄적으로 취소가 되어짐. ( cancel이 전파됨. )
// 즉, hierarchy 구조로 실행되던 모든 코루틴들이 취소가 됨.
fun main() = runBlocking<Unit> {
    try {
        failedConcurrentSum()
    } catch(e: ArithmeticException) {
        println("Computation failed with ArithmeticException")
    }
}

suspend fun failedConcurrentSum() : Int = coroutineScope {
    val one = async<Int> {
        try {
            delay(Long.MAX_VALUE)
            42
        } finally {
          println("first child was cancelld")
        }
    }
    val two = async<Int> {
        println("second child throws an exception")
        throw ArithmeticException()
    }
    one.await() + two.await()
}
**/
