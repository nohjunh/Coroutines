package com.example.study

import kotlinx.coroutines.*

/**
// 코루틴을 취소하는게 리소스 활용에 중요한 요소.
fun main() = runBlocking { // 메인스레드를 Blocking
    val job = launch {
        // 1000번 반복
        repeat(1000) { i ->
            println("job: i'm sleeping $i")
            delay(500L) // 0.5초 간격
        }
    }
    // launch할 때 받은 job객체는 cancel 시킬 수 있다.
    delay(1300L)
    println("main: I'm tired of waiting!")
    job.cancel() // cancel the job ( 코루틴 종료 )
    job.join() // waits for job's completion
    println("main: Now i can quit.")
}
**/

/**
// 코루틴을 완벽히 취소하려면 cancel을 check 해야 된다.
// suspend function은 취소가능
// 아래 예제는 코루틴이 취소가 되지 않음.
// why? suspend function이 없기 때문 (delay suspend function이라도 있어야 함.)
// delay()를 사용하지 않고 할 수 있는 방법 -> yield() 사용
fun main() = runBlocking {
    val startTime = System.currentTimeMillis()
    val job = launch(Dispatchers.Default) { // 코루틴 생성
        var nextPrintTime = startTime
        var i = 0
        while (i < 5) {
            if(System.currentTimeMillis() >= nextPrintTime) {
                yield()
                println("job: I'm sleeping ${i++}")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L) // 1.3초 뒤에 캔슬
    println("main: I'm tried of waiting!")
    job.cancelAndJoin() // 캔슬과 조인을 순차적으로 실행시키는 함수
    // 코루틴을 취소 시킨다는건 강제로 Exception을 발생시켜서 코루틴을 중단시키는 원리
    // 그러나, 코루틴 내부에서 exception을 체크할 수 있는 어떠한 suspend function이 없다면
    // 코루틴은 완전히 완료되기 전까지는 중간에 취소 되지 않는다.
    println("main: Now I can quit.")
}
**/

/**
// 위 방법은 주기적으로 suspend function을 호출하면서 코루틴이 cancel 됐는지 확인하는 방법
// 아래 방법은 명시적으로 isActive 상태를 확인해 active상태가 아니면 코루틴 중단시키는 방법
// 아래 방법이 더 유연한 방법. -> Exception을 던지지 않기 때문
// 반면, 아까 위에서 한 방법은 exception을 던지면서 exception을 체크하고 종료시키는 방법
fun main() = runBlocking {
    val startTime = System.currentTimeMillis()
    val job = launch(Dispatchers.Default) { // 코루틴 생성
        var nextPrintTime = startTime
        var i = 0
        while (isActive) { // 매 while 시마다 isActive를 통해 상태를 체크
            if(System.currentTimeMillis() >= nextPrintTime) {
                println("job: I'm sleeping ${i++}")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L) // 1.3초 뒤에 캔슬
    println("main: I'm tried of waiting!")
    job.cancelAndJoin() // 캔슬과 조인을 순차적으로 실행시키는 함수
    println("main: Now I can quit.")
}
**/

/**
// 코루틴이 중간에 취소 될 경우 finally 작업을 하도록 만들어야 함.
// ex) DB작업 중이거나 파일을 닫고 끝내야 한다던가 등등
fun main() = runBlocking {
    val job = launch {
        try {
            repeat(1000) { i ->
                println("job: I'm sleeping $i")
                delay(500L) // suspend function으로 취소를 체크할 떄 리소스해제는 finally 블럭에서 하면 됨.
            }
        } finally { // 이 블럭에서 리소스 해제 구문이 들어가면 됨.
            println("job: I'm running finally")
        }
    }
    delay(1300L) // 1.3초 뒤에 종료
    println("main: I'm tried of waiting!")
    job.cancelAndJoin() // join에 의해 코루틴이 완전히 종료될 때까지 대기
    println("main: Now I can quit.")
}
**/

/**
// Run non-cancellable bloc
// cancel 실행해서 코루틴을 취소 시켰는데 이 코루틴 내부에서 suspend function을 불러야 하는 경우
// withContext에 NonCancellable을 넣어주면
// 끝난 코루틴에서도 finally에서 코루틴을 실행시켜서 추가작업을 할 수 있다.
// 단, 이런 작업은 rare한 case
fun main() = runBlocking {
    val job = launch {
        try {
            repeat(1000) { i ->
                println("job: I'm sleeping $i")
                delay(500L) // suspend function으로 취소를 체크할 떄 리소스해제는 finally 블럭에서 하면 됨.
            }
        } finally { // 이 블럭에서 리소스 해제 구문이 들어가면 됨.
            withContext(NonCancellable) {
                println("job: I'm running finally")
                delay(1000L)
                println("job: And I've just delayed for 1 sec")
            }
        }
    }
    delay(1300L) // 1.3초 뒤에 종료
    println("main: I'm tried of waiting!")
    job.cancelAndJoin() // join에 의해 코루틴이 완전히 종료될 때까지 대기
    println("main: Now I can quit.")
}
**/

/**
// Timeout
// launch된 코루틴의 job을 캔슬하는게 아니라 코루틴 실행하면서 일정 시간이 지나면 취소되도록 함
// 아래 예제는 timeout이 되면 exception을 던지면서 죽음
fun main() = runBlocking {
    withTimeout (1300L) { // 1.3초 뒤에 코루틴 timeout
        repeat(1000) { i ->
            println("I'm sleeping $i")
            delay(500L)
        }
    }
}
**/

/**
// 아래 예제는 코루틴이 timeout이 되면 null을 반환함.
fun main() = runBlocking {
    val result = withTimeoutOrNull(1300L) {
        repeat(1000) { i ->
            println("I'm sleeping $i")
            delay(500L)
        }
        "Done" // "Done"으로 끝맺음
    }
    println("Result is $result") // result값을 찍었을 때 null 출력
}
**/

/**
 * 정리
 1. Job -> cancel() // 캔슬한다고해서 코루틴이 종료되는게 아님. 코루틴 코드 자체에서 협조적인 취소에 관한 코드가 있어야 함.
 2. Cancellation is cooperative //취소는 협조적이어야 함.
    way 1. to periodically invoke a suspending (suspend function실행 되고 후에 코루틴이 재개될 떄 취소되었는지 체크함)
    way 2. explicitly check the cancellation status (isActive)
 3. Timeout (알아서 특정시간 후에 캔슬)
    1. withTimeout
    2. withTimeoutOrNull
 **/
