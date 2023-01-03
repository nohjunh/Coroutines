package com.example.study

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
ref)
https://kotlinlang.org/docs/flow.html
https://jaejong.tistory.com/67

suspend function은 단일 값을 반환하지만,
Flow Builder를 통해 다중 값 반환하는 비동기 function을 만들 수 있다.
Multiple values= ex) ArrayList, List, Map, Set ...
 */


////// 동기 방식 /////////////
/*
fun foo(): ArrayList<Int> {
    var arrayList = ArrayList<Int>()
    arrayList.add(1)
    arrayList.add(2)
    arrayList.add(3)
    return arrayList
}

fun main(){
    var test = foo()
    test.forEach {
      value -> println(value)
    }
}
*/
//////////////코루틴 비동기/////////////////////////
/*
suspend fun simple(): List<Int> {
    delay(3000) // pretend we are doing something asynchronous here
    return listOf(1, 2, 3)
}

fun main() = runBlocking<Unit> {
    println("start")
    simple().forEach { value -> println(value) }
}
*/


/////////////////Flow Builder////////////////////////////
/*
flow builder( flow{} )로 만든 simple 비동기 function이 launch로 생성된 코루틴과
함께 수행됨.
flow{}를 통해 Flow type function simple() 생성

-- 결과:
I'm not blocked 1
1
I'm not blocked 2
2
I'm not blocked 3
3
*/
/*
fun simple(): Flow<Int> = flow { // flow builder
    for (i in 1..3) {
        delay(100) // pretend we are doing something useful here
        emit(i) // emit next value
        // 결과값을 emit()를 통해 collect함수 호출부로 방출(emit)
        // 방출된 value들은 collect함수를 호출부에서 수집(collect)
    }
}

fun main() = runBlocking<Unit> {
    // Launch a concurrent coroutine to check if the main thread is blocked
    launch {
        for (k in 1..3) {
            println("I'm not blocked $k")
            delay(100)
        }
    }
    // Collect the flow
    // flow 내부 결과는 collect()로 수집해 가져올 수 있음
    simple().collect { value -> println(value) }
}
*/


//////////// ColdStream //////////////
/*
Flow는 콜드 스트림
flow builder 내부의 코드는 flow가 collect()할 때까지 실행되지 않음.

 -- 결과
Calling simple function...
Calling collect...
Flow started
1
2
3
Calling collect again...
Flow started
1
2
3
 */
/*
fun simple(): Flow<Int> = flow {
    println("Flow started")
    for (i in 1..3) {
        delay(100)
        emit(i)
    }
}

fun main() = runBlocking<Unit> {
    println("Calling simple function...")
    val flow = simple()
    println("Calling collect...")
    flow.collect { value -> println(value) } // simple() 수행
    println("Calling collect again...")
    flow.collect { value -> println(value) } // simple() 수행
}
*/



////////// .asFlow() //////////
/*
// 이미 선언된 Collection 객체들을 Flow로 변환 가능
// Convert an integer range to a flow
suspend fun main() {
    (1..3).asFlow().collect { value -> println(value) }
}
*/

/*
///////// reduce() //////////
// map을 맨 처음 원소부터 돌면서 연산값을 누적시킴.
fun main() = runBlocking<Unit> {
    val sum = (1..5).asFlow()
        .map { it * it } // squares of numbers from 1 to 5
        .reduce { a, b -> a + b } // sum them (terminal operator)
    println(sum)
}
*/

/*
///////// reduce() //////////
// map을 맨 처음 원소부터 돌면서 연산값을 누적시킴.
fun main() = runBlocking<Unit> {
    val sum = (1..5).asFlow()
        .map { it * it } // squares of numbers from 1 to 5
        .reduce { a, b -> a + b } // sum them (terminal operator)
    println(sum)
}
*/
