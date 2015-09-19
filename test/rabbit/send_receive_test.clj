(ns rabbit.send_receive_test
  (:require [clojure.test :refer :all]
            [rabbit.send-receive :refer :all])
  (:refer-clojure :exclude [send]))

(deftest send-receive
  (testing "send message"
    (send))
  (testing "send message again"
    (send))
  (testing "receive message"
    (receive)))
