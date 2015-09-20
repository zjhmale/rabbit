(ns rabbit.task-worker
  (:require [langohr.core :as lc]
            [langohr.channel :as lch]
            [langohr.queue :as lq]
            [langohr.basic :as lb]
            [clojure.string :as s]
            [rabbit.config :as config]
            [langohr.consumers :as lcons])
  (:import (java.util.concurrent Executors))
  (:refer-clojure :exclude [send]))

(def ^{:const true} queue-name "task_queue")

(defn send
  [payload]
  (with-open [conn (lc/connect {:host                  config/rabbitmq-host
                                :port                  config/rabbitmq-port
                                :username              config/rabbitmq-username
                                :password              config/rabbitmq-password
                                :automatically-recover true
                                :executor              (Executors/newFixedThreadPool config/rabbitmq-thread-number)})]
    (let [ch (lch/open conn)]
      ;;假如rabbitmq自己挂掉了那么消息如果不落盘都会丢失 所以要加上durable和persistent都为true的设置
      (lq/declare ch queue-name {:durable     true
                                 :auto-delete false})
      (lb/publish ch "" queue-name payload {:persistent true})
      (prn (format " [x] Send %s" payload)))))

(defn ^:private occurences-of
  [^String s ^Character c]
  (let [chars (map identity s)]
    (->> chars
         (filter (fn [x]
                   (= x c)))
         count)))

(defn handle-delivery
  "message handler callback"
  [ch {:keys [delivery-tag]} payload]
  (let [s (String. payload "UTF-8")
        n (occurences-of s \.)]
    (locking *out*
      (prn (format " [x] Received %s" s)))
    (Thread/sleep ^Double (* 1000 n))
    (prn " [x] Done")
    ;;真实情况下某一个worker可能会crash掉 如果不回传一个ack给rabbitmq 那么这个worker分配到的未做完的消息任务就都丢失了
    ;;如果有ack机制可以提醒rabbitmq将未处理的消息再调度给其他的worker
    ;;An ack(nowledgement) is sent back from the consumer to tell RabbitMQ that a particular message had been received, processed and that RabbitMQ is free to delete it.
    ;;If consumer dies without sending an ack, RabbitMQ will understand that a message wasn't processed fully and will redeliver it to another consumer.
    ;;Message acknowledgments are turned on by default.
    (lb/ack ch delivery-tag)))

(defn receive []
  (with-open [conn (lc/connect {:host                  config/rabbitmq-host
                                :port                  config/rabbitmq-port
                                :username              config/rabbitmq-username
                                :password              config/rabbitmq-password
                                :automatically-recover true
                                :executor              (Executors/newFixedThreadPool config/rabbitmq-thread-number)})]
    (let [ch (lch/open conn)]
      (lq/declare ch queue-name {:durable     true
                                 :auto-delete false})
      ;;因为rabbitmq的调度机制很有可能会把很多大量消耗资源的任务分配到某几个worker上 而其他的worker分配到的任务很少 就会造成这几个worker压力过大的问题
      ;;This tells RabbitMQ not to give more than one message to a worker at a time.
      ;;Or, in other words, don't dispatch a new message to a worker until it has processed and acknowledged the previous one.
      ;;Instead, it will dispatch it to the next worker that is not still busy.
      (lb/qos ch 1)
      (locking *out*
        (prn " [*] Waiting for messages. To exit press C-c"))
      (lcons/blocking-subscribe ch queue-name handle-delivery))))
