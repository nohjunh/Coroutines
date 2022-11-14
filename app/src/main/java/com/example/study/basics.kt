package com.example.study

import kotlinx.coroutines.*

/**
 -------------예제1----------
fun main() {
    /**
    launch = 코루틴빌더(launch를 통해 내부적으로 코루틴이 반환됨)
    -> globalScope객체(=전역scope) 안에서 코루틴이 실행
     코루틴은 light-weight 스레드라고 보면 됨. (스레드로 해도 동일하게 동작함.)
     **/
    GlobalScope.launch {
        delay(1000L) // delay는 서스펜드function
        println("World!")
    }
    println("Hello,")
    runBlocking { // runBlocking도 마찬가지로 코루틴빌더
        Thread.sleep(2000L)
    }

    // 좀 더 관용적인 형태
    // runBlocking 안에 있는 구문이 다 실행되기 전까지
    // main함수가 리턴이 안될 것이다.
    runBlocking {
        GlobalScope.launch {
            delay(1000L) // delay는 서스펜드function
            println("World!")
        }
        println("Hello,")
        delay(2000L)
    }
}
 **/

/**
---------------예제 2--------------
fun main() = runBlocking {
    // job 객체
    val job = GlobalScope.launch {
        delay(3000L)
        println("World !")
    }
    println("Hello,")
    // job객체에다가 join메소드를 하면 job이 완료될 때까지 메인함수가 종료되지 않고 기다림.
    job.join()
}
**/
/**
---------------예제3-----------
// Structured concurrency
fun main() = runBlocking {
    // runBlocking에서 들어온 코루틴Scope로부터
    // launch를 하여 코루틴빌더를 생성
    // 이렇게 하면 각 코루틴마다 job 객체를 만들어서 각각 join을 할 필요가 없다.
    // 부모코루틴인 runBlocking의 child로 launch를 이용한 코루틴을 만들면
    // 부모코루틴이 child 코루틴이 완료되는 것까지 알아서 기다려줌.
    launch {
        delay(1000L)
        println("World !")
    }
    // 굳이 this.launch로 안해도 됨.
    launch {
        delay(1000L)
        println("World !")
    }
    println("Hello,")
}
**/

/**
//---------예제4---------
fun main() = runBlocking {
    launch {
        doWorld()
    }
    println("Hello, ")
}

// suspend 키워드를 붙여줘서 doWorld함수도 일시 중단이 가능한 함수로 만들어줌.
// suspend function은 코루틴이나 suspend function안에서만 호출될 수 있다.
suspend fun doWorld() {
    delay(1000L)
    println("World!")
}
**/

/**
 코루틴이 스레드보다 light하다.
 Global coroutines are like daemon threads
 즉, 코루틴이 무조건적인게 아니라 프로세스가 살아있을 때만(=메인함수가 살아있을 때만) 코루틴이 동작한다.
 프로세스가 끝나면 코루틴이 돌아가고 있어도 코루틴이 끝난다.
**/

/**
// 이렇게 실행하게 되면,
// 스케줄링 관점에서 runBlocking -> 첫번째 launch -> 두번째 launch 이렇게 쌓이게 된다.
// 따라서, Coroutine Outer가 먼저 나오게 되고
// 두 번째로 첫번째 launch가 나오게 되고 delay시 suspend되므로 두번 째 launch가 실행되는 구조
// 두 번쨰 launch가 suspend되면 첫 번째 launch가 resume
// 즉, suspend <-> resume이 반복되는 형태
fun main() = runBlocking {
    launch {
        repeat(5) { i ->
            println("Coroutine A, $i")
            delay(10L)
        }
    }

    launch {
        repeat(5) { i ->
            println("Coroutine B, $i")
            delay(10L)

        }
    }
    println("Coroutine Outer")
}
**/

/**
// 정리
 Coroutine builder // 코루틴 생성 및 실행
 1. launch
 2. runBlocking
 Scope // 빌더들은 scope안에서 실행 됨.
 1. GlobalScope -> lifetime이 전역
 2. CoroutineScope
 Suspend function // 일시중단 함수
 1. suspend
 2. delay() -> delay함수도 suspend function(코루틴을 일시중지시킴)
 3. join()
 Structured cocurrency
**/

