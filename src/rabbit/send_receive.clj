(ns rabbit.send-receive
  (:require [langohr.core :as lc]
            [langohr.channel :as lch]
            [langohr.queue :as lq]
            [langohr.basic :as lb]
            [langohr.consumers :as lcons]
            [rabbit.config :as config])
  (:import (java.util.concurrent Executors))
  (:refer-clojure :exclude [send]))

(defn send []
  (with-open [conn (lc/connect {:host                  config/rabbitmq-host
                                :port                  config/rabbitmq-port
                                :username              config/rabbitmq-username
                                :password              config/rabbitmq-password
                                :automatically-recover true
                                :executor              (Executors/newFixedThreadPool config/rabbitmq-thread-number)})]
    (let [ch (lch/open conn)]
      (lq/declare ch "queue_name" {:durable     false
                                   :auto-delete false})
      (lb/publish ch "" "queue_name" (.getBytes "message" "UTF-8"))
      (prn " [x] Send 'message'"))))

(defn handle-delivery
  "message handler callback"
  [ch metadata payload]
  (prn (format " [x] Receive '%s'" (String. payload "UTF-8"))))

(defn receive []
  (with-open [conn (lc/connect {:host                  config/rabbitmq-host
                                :port                  config/rabbitmq-port
                                :username              config/rabbitmq-username
                                :password              config/rabbitmq-password
                                :automatically-recover true
                                :executor              (Executors/newFixedThreadPool config/rabbitmq-thread-number)})]
    (let [ch (lch/open conn)]
      (lq/declare ch "queue_name" {:durable     false
                                   :auto-delete false})
      (prn " [*] Waiting for messages. To exit press C-c")
      (lcons/blocking-subscribe ch "queue_name" handle-delivery {:auto-ack true}))))