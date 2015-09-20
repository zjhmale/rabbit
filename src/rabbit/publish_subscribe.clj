(ns rabbit.publish-subscribe
  (:require [langohr.core :as lc]
            [langohr.channel :as lch]
            [langohr.exchange :as le]
            [langohr.basic :as lb]
            [langohr.queue :as lq]
            [langohr.consumers :as lcons]
            [rabbit.config :as config])
  (:import (java.util.concurrent Executors))
  (:refer-clojure :exclude [send]))

;;The core idea in the messaging model in RabbitMQ is that the producer never sends any messages directly to a queue.
;;Actually, quite often the producer doesn't even know if a message will be delivered to any queue at all.
;;生产者将消息发给exchange exchange再将消息放入队列中
;;如果用控字符串表示exchange的那么那就是用默认的exchange

;;一个producer可以通过exchange将消息发送到不同的队列中,只要这个队列是bind到这个exchange上的

(def ^{:const true} exchange-name "logs")

(defn emit-log
  [payload]
  (with-open [conn (lc/connect {:host                  config/rabbitmq-host
                                :port                  config/rabbitmq-port
                                :username              config/rabbitmq-username
                                :password              config/rabbitmq-password
                                :automatically-recover true
                                :executor              (Executors/newFixedThreadPool config/rabbitmq-thread-number)})]
    (let [ch (lch/open conn)]
      ;;试用fanout类型的exchange
      (le/fanout ch exchange-name {:durable     false
                                   :auto-delete false})
      (lb/publish ch exchange-name "" payload)
      (prn (format " [x] Sent %s" payload)))))

(defn handle-delivery
  "message handler callback"
  [ch metadata payload]
  (prn (format " [x] %s" (String. payload "UTF-8"))))

(defn receive-log []
  (with-open [conn (lc/connect {:host                  config/rabbitmq-host
                                :port                  config/rabbitmq-port
                                :username              config/rabbitmq-username
                                :password              config/rabbitmq-password
                                :automatically-recover true
                                :executor              (Executors/newFixedThreadPool config/rabbitmq-thread-number)})]
    (let [ch (lch/open conn)
          {:keys [queue]} (lq/declare ch "" {:durable     true
                                             :auto-delete false})]
      (le/fanout ch exchange-name {:durable     false
                                   :auto-delete false})
      ;;将队列bind到exchange上
      (lq/bind ch queue exchange-name)
      (prn " [*] Waiting for logs. To exit press C-c")
      (lcons/blocking-subscribe ch queue handle-delivery))))