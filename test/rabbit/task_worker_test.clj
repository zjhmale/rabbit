(ns rabbit.task-worker-test
  (:require [clojure.test :refer :all]
            [rabbit.task-worker :refer :all])
  (:refer-clojure :exclude [send]))

(deftest task-worker
  (testing "send messages"
    (send "First message.")
    (send "Second message..")
    (send "Third message...")
    (send "Fourth message....")
    (send "Fifth message....."))
  (testing "receive message"
    (future (receive))
    (future (receive))
    ;;等到worker都消费掉了消息再结束主线程
    (Thread/sleep 8000)))

