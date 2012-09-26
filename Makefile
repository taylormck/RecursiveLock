JUNIT_PATH=/usr/share/java/junit.jar
BIN_PATH=./bin
ALL_PATH=./:$(JUNIT_PATH):$(BIN_PATH)

all: one_simple_thread two_simple_threads instance_count violation chaos
	
chaos:
	java -cp $(ALL_PATH) org.junit.runner.JUnitCore cs439.lab2.lock.Test_Chaos 2> Test_Chaos.student

instance_count:
	java -cp $(ALL_PATH) org.junit.runner.JUnitCore cs439.lab2.lock.Test_InstanceCount 2> Test_InstanceCount.student
	diff Test_InstanceCount.student  outputs/Test_InstanceCount.output

one_simple_thread:
	java -cp $(ALL_PATH) org.junit.runner.JUnitCore cs439.lab2.lock.Test_OneSimpleThread 2> Test_OneSimpleThread.student
	diff Test_OneSimpleThread.student  outputs/Test_OneSimpleThread.output


two_simple_threads:
	java -cp $(ALL_PATH) org.junit.runner.JUnitCore cs439.lab2.lock.Test_TwoSimpleThreads 2> Test_TwoSimpleThreads.student
	diff Test_TwoSimpleThreads.student  outputs/Test_TwoSimpleThreads.output

violation:
	java -cp $(ALL_PATH) org.junit.runner.JUnitCore cs439.lab2.lock.Test_Violation 2> Test_Violation.student
	diff Test_Violation.student  outputs/Test_Violation.output

turnin_setup:
	tar -cvf proj2_`whoami`.tar.gz README src/student/RecursiveLock.java

turnin: turnin_setup
	turnin --submit yjkwon proj2_rockhold proj2_`whoami`.tar.gz

clean:
	rm -rf ./*.student


