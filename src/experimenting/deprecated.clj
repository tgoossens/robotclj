(ns robotnxt.core
	 (:gen-class))


(defrecord dimensions [lightsensor-offset proximity-offset wheel-distance height width depth])

(defrecord sensor [type value])

(defrecord status [movement position orientation])

(defrecord robot [sensors motor dimensions status])

(defrecord command [type data])



(def serialize 
{:forward 0x000000
 :backward 0x0010101})

(defn virtual []
	"Simulator"
  {:enabled true
   :next-cmd nil
   :current-cmd nil
   :clockrate 1
   :move-step 0.5
   :rotate-step 0.5})



(defn newcommand? [virtual] "Has the virtual a next command to execute?" (boolean (:next-cmd virtual)))


(def virtuals (atom  {}))


(defn send-bluetooth [destination message])


(defn send-virtual [virt-id cmd]
		(swap! virtuals assoc virt-id (assoc (@virtuals virt-id) :next-cmd cmd)))


(defmulti send-command (fn [robot command] (:type robot)))
;NXT

(defmethod send-command :nxt [robot command]
	"robot must have :bluetooth address"
	(send-bluetooth (:bt-mac robot) command)) ;over bluetooth

(defmethod send-command :virtual [robot command]
	"robot must have :virtual id "
	(send-virtual (:virtual robot) command))


(def commands 
	#{:drive
	 :rotate
	 :travel
	 :turn
	 :nop })

;COMMANDS

(defn serializedata [type data])
;CONTROL ROBOT

(defn drive [robot direction]
	(send-command robot direction))

(defn rotate [robot direction]
	(send-command robot direction))

(defn travel [robot distance direction]
	(send-command (serializedata :travel )))
(defn turn [robot degrees direction])


(def requests 
	#{::status
	 ::lightsensor
	 ::proximity})

(defrecord request [req robot promise-value])

(def pending-requests "Contains pending requests" (ref {}))

;REQUEST COMMANDS

(defn req-status [robot]
	"Add request id. To find it back"
	(let [id 0]
		(dosync 
		 (alter pending-requests assoc id (->request ::status robot (promise))))
		(send-command robot ::status)))



;fulfill promise

(defn fulfill-req [{:keys [id value]}]
	(let [p (:promise-value (@pending-requests id)) ]
		(deliver p value)))

(defn answered? [req]
	"Returns wether a request has been answered"
	(realized? (:promise-value req)))


;LISTENER-PROCESS




;VIRTUAL

(defn create-virtual-robot [virtual-id] {
	:type :virtual
	:virtual virtual-id
	:status {:position [0 0]
		       :orientation 45
		       :rotate-speed 30
		       :drive-speed 10}})


(defn register-virtual [virtuals virtual]
	"returns id"
	(swap! virtuals assoc 0 virtual)
   0)

(defn add-vector [v1 v2]
	(map + v1 v2))

(defn deg->rad [deg]
	(Math/toRadians deg))

(defn v-drive [{:keys [virtual] :as robot}]
  (let [v (@virtuals virtual)
  	    status (:status robot)
  	    angle (deg->rad (:orientation status))
  	    move-step (:move-step v)]
  	    (assoc-in robot [:status :position]
  	          (add-vector 
  		        	(:position status)
  		          [(* move-step (Math/cos angle)) (* move-step (Math/sin angle))]))))


(defn v-turn [{:keys [virtual] :as robot}]
  (let [v (@virtuals virtual)
  	    status (:status robot)
  	    angle (deg->rad (:orientation status))
  	    rotate-step (:rotate-step v)]
  	    (assoc-in robot [:status :orientation]
  	    	(+ rotate-step (:orientation status)))))
  	          
  		       


(defn execute-action [robot action rate stopcondition]
	"action and stopcondition must be fn that can have robot as parameter. 
	Action must return the next state of the robot.
	Rate is the amount of action calls per second."
   (loop []
    (when-not (stopcondition @robot)
     (do 
   	  (Thread/sleep (/ 1000 rate))
	    (swap! robot action)
	    (println "robot:" @robot)
	    (recur)))))



(defn stat [robot property] "Return given status property" (property (:status robot)))

;STOP CONDITIONS
(defn turned-degrees? [robot degrees] 
	"Returns a function that accepts a new state of the robot. This fn returns true if the 
	difference in orientation between the intial state and the given state exceeds the given 
	amount of degrees."
	(fn [nextrobot]
		(>= (- (stat nextrobot :orientation)  (stat robot :orientation)) degrees)))

(defn action-rate [clockrate speed stepsize]
	(* clockrate (/ speed stepsize)))

(defn -main [& args]
	(let [v  (virtual)
		    virt-id (register-virtual virtuals v)
		    robot (atom (create-virtual-robot virt-id))]   
  (execute-action 
  	robot 
  	v-turn 
  	(action-rate (:clockrate v) (-> @robot :status :rotate-speed) (:rotate-step v))
  	(turned-degrees? @robot 30))

  (execute-action 
  	robot 
  	v-turn 
  	(action-rate (:clockrate v) (-> @robot :status :rotate-speed) (:rotate-step v))
  	(turned-degrees? @robot 25))
  

  (execute-action robot v-drive (action-rate (:clockrate v) (-> @robot :status :drive-speed) (:move-step v)) 
  	(fn [robot] 
  		false))))


;VIRTUAL-LISTENER

;(defn start-virtual-listeners []
;	"If virtual changed. "
;	(add-watch virtuals nil (fn [k r o n]
;		(let [v (filter newcommand? n)]
;		 (map #(assoc % :current-cmd (:next-cmd %) :next-cmd nil) v)))))












