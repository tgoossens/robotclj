(ns robotclj.core
  (:require [robotclj.virtual :as v]))

                                        ;abstractions for controlling a robot.





(defn virtual-executor [simulator]
  "Will dispatch the given command to the given virtual simulator, which is a reference (agent)"
  {:travel (fn [distance direction] )
   :drive (fn [direction] (swap! ))
   :rotate (fn [rotation] rotation)
   :turn (fn [angle rotation] rotation)
   :whiteline (fn [] )
   :center (fn [])
   :explore (fn [])
   :stop (fn [])})

(defn remote-executor [dispatch-fn]
  "Will dipatch the given command to a remote (bluetooth) listener. It will return true when the command has
        been succesfully executed."
  {:travel (fn [distance direction] direction)
   :drive (fn [direction] direction)
   :rotate (fn [rotation] rotation)
   :turn (fn [angle rotation] rotation)
   :whiteline (fn [] )
   :center (fn [])
   :explore (fn [])
   :stop (fn [])})


                                        ;controll a robot
                                        ;merge executor with pilot
                                        ;maybe this can be a request as well. and the receiver gets notified when the action is complete?


(defn execute [robot action]
  (-> robot :executor action))

(defn drive [robot direction]
  "Indefinite driving of robot."
  ((execute robot :drive) direction))

(defn rotate [robot rotation]
  "Indefinite rotating of robot."
  ((execute robot :rotate) rotation))

(defn travel [robot distance direction]
  "Fixed distance travelling of the robot. Returns true when finished."
  ((execute robot :travel) distance direction))

(defn turn [robot angle rotation]
  "Fixed angle turning of the robot. Returns true when finished"
  ((execute robot :turn) angle rotation))

(defn whiteline [robot]
  "Orient robot perpendicular to white line. Returns true when finished."
  ((execute robot :whiteline)))

(defn center [robot]
  "Center the robot the sector. Returns true when finished"
  ((execute robot :center)))

                                        ;what about intermediate information? About what nodes have been discovered? How do get this information?

(defn explore [robot]
  "Make robot start autonomous maze exploring. Returns true when finished."
  ((execute robot :explore)))


(defn read-lightsensor [robot]
  "Returns the value of the lightsensor"
  ((-> robot :lightsensor :read)))

(defn read-proximitysensor [robot]
  "Returns the value of the proximitysensor"
  ((-> robot :proximitysensor :read)))


;;ROBOT

                                        ;lightsensor

(defn virtual-lightsensor [virtual]
  "A virtual lightsensor."
  {:read (fn [] 0)} )

(defn remote-lightsensor [dispatch-fn]
  "A remote lightsensor. Will use dispatch function to send information via bluetooth."
  {:read (dispatch-fn :lightsensor)})

                                        ;proximity sensor

(defn virtual-proximitysensor [virtual]
  {:read (fn [] 255)} )

(defn remote-proximitysensor [dispatch-fn]
  {:read (dispatch-fn :lightsensor)})




                                        ;some notes
                                        ;
;;The robot is made up of components
 ;                                       ;The virtual that is given to each component containts information.
                                        ;The virtual knows the position, orientation, speed (?), ;
                                        ;
                                        ;The virtual can be sent commands.


(defn -main [& args]
  (let [simulator (atom (v/create-simulator [0 0] 0 1 1))
        virtual {:lightsensor (virtual-lightsensor simulator)
                 :proximitysensor (virtual-proximitysensor simulator)
                 :executor (virtual-executor simulator)}]

    (.start (Thread. (fn [] (v/start-simulator simulator))))
    (drive virtual :forward)
    (loop []
      (Thread/sleep 1000)
      (println (read-lightsensor virtual))
      (recur))))


(comment
  (def v (virtual-robot))
  (drive v :forward)
  (travel v 20 :forward)
  (read-proximitysensor v))
