(ns rabbit.publish-subscribe-test
  (:require [clojure.test :refer :all]
            [rabbit.publish-subscribe :refer :all]))

(deftest publish-subscribe
  (testing "receive log"
    (future (receive-log)))
  (testing "emit log"
    (Thread/sleep 3000)
    (emit-log "log1")
    (emit-log "log2")
    (emit-log "log3")))
