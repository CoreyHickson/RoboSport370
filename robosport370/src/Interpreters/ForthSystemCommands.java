package Interpreters;

import java.util.Random;
import java.util.Stack;

import Exceptions.ForthParseException;
import Exceptions.ForthRunTimeException;
import Models.ForthBoolLiteral;
import Models.ForthIntegerLiteral;
import Models.ForthPointerLiteral;
import Models.ForthStringLiteral;
import Models.ForthWord;
import Models.Robot;

public class ForthSystemCommands {
    /**
     * This class will run the standard forth commands
     */
    
    
    
    /**
     * returns the robot’s current health (1–3)
     *  ( -- i )
     * @param forthStack     the stack for the currently running forth program
     * @param robot
     */
    protected static void health(Stack<ForthWord> forthStack, Robot robot) {
        ForthWord result;
         long health = robot.getHealth();
         result = new ForthIntegerLiteral(health);
         forthStack.push(result);
    }

    /**
     * returns the robot’s range of movement (0–3)
     * ( -- i)
     * 
     * @param forthStack     the stack for the currently running forth program
     * @param movesLeft
     */
    protected static void movesLeft(Stack<ForthWord> forthStack, long movesLeft) {
        ForthWord result;
         result = new ForthIntegerLiteral(movesLeft);
         forthStack.push(result);
    }

    /**
     * returns the robot’s firepower (1–3)
     * ( -- i )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @param robot
     */
    protected static void firePower(Stack<ForthWord> forthStack, Robot robot) {
        ForthWord result;
        long strength = robot.getStrength();
        result = new ForthIntegerLiteral(strength);
        forthStack.push(result);
    }

    /**
     * returns the robot’s team number
     *  ( -- i )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @param robot
     */
    protected static void teamNumber(Stack<ForthWord> forthStack, Robot robot) {
        ForthWord result;
        long team = robot.getTeamNumber();
        result = new ForthIntegerLiteral(team);
        forthStack.push(result);
    }

    /**
     * returns the robot’s member number
     *  ( -- i )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @param robot
     */
    protected static void memberNumber(Stack<ForthWord> forthStack, Robot robot) {
        ForthWord result;
        long member = robot.getMemberNumber();
        result = new ForthIntegerLiteral(member);
        forthStack.push(result);
    }

    /**
     * prints the current value out, using the same syntax as they would be input with
     *  ( v -- )
     * 
     * @param forthStack     the stack for the currently running forth program
     */
    protected static void console(Stack<ForthWord> forthStack) {
        ForthWord first;
        first = forthStack.pop();
        String consoleString = first.consoleFormat();
        System.out.println(consoleString);
    }

    /**
     * generates a random integer between 0 and i inclusive
     * ( i -- )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void random(Stack<ForthWord> forthStack) throws ForthRunTimeException {
        ForthWord first;
        first = forthStack.pop();
        if(first instanceof ForthIntegerLiteral && ((ForthIntegerLiteral)first).getValue() >= 0){
            int i = (int)((ForthIntegerLiteral)first).getValue();
            Random rand = new Random();
            long r = rand.nextInt(i+1);
            ForthIntegerLiteral newWord = new ForthIntegerLiteral(r);
            forthStack.push(newWord);   
        } else {
            throw new ForthRunTimeException("random word called without a positive int on top of the stack");
        }
    }

    /**
     * send a value v to team-member i; returns a boolean indicating success or failure
     *  ( i v -- b )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void sendMail(Stack<ForthWord> forthStack) throws ForthRunTimeException {
        ForthWord first;
        ForthWord second;
        first = forthStack.pop();
        second = forthStack.pop();
        if((first instanceof ForthIntegerLiteral || first instanceof ForthBoolLiteral || first instanceof ForthStringLiteral) && second instanceof ForthIntegerLiteral){
            int memberNumber = (int)((ForthIntegerLiteral)second).getValue();
           // TODO: After game controller is set up
        } else {
            throw new ForthRunTimeException("attempting to send mailbox without the proper stack format");
        }
    }

    /**
     * indicates whether the robot has a waiting message from team-member i
     *  ( i -- b )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @param robot
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void checkMail(Stack<ForthWord> forthStack, Robot robot) throws ForthRunTimeException {
        ForthWord first;
        ForthWord result;
            first = forthStack.pop();
            if(first instanceof ForthIntegerLiteral){
                int memberNumber = (int)((ForthIntegerLiteral)first).getValue();
                boolean hasMail = robot.hasMailFromMember(memberNumber);
                result = new ForthBoolLiteral(hasMail);
                forthStack.push(result);
            } else {
                throw new ForthRunTimeException("attempting to check mailbox without an int on top of the stack");
            }
    }

    /**
     * pushes the next message value onto the stack.
     *  ( i -- v )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @param robot
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void recieveMail(Stack<ForthWord> forthStack, Robot robot) throws ForthRunTimeException {
        ForthWord first;
        first = forthStack.pop();
        if(first instanceof ForthIntegerLiteral){
            int memberNumber = (int)((ForthIntegerLiteral)first).getValue();
            ForthWord mail = robot.popMailFromMember(memberNumber);
            forthStack.push(mail);
        } else {
            throw new ForthRunTimeException("attempting to get mail without an int on top of the stack");
        }
    }

    /**
     * takes a variable and returns the value it’s storing
     *  ( p -- v )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @param robot
     * @throws ForthParseException
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void checkVariable(Stack<ForthWord> forthStack, Robot robot)
            throws ForthParseException, ForthRunTimeException {
        ForthWord first;
        ForthWord result;
        first = forthStack.pop();
        if(first instanceof ForthPointerLiteral){
            String value = ((ForthPointerLiteral) first).getVariableValue(robot);
            result = ForthParser.wordFromString(value, robot);
            forthStack.push(result);
        } else {
            throw new ForthRunTimeException("? word called without a variable pointer on top of the stack");
        }
    }

    /**
     * stores a new value into a pointer
     * ( v p -- )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @param robot
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void assignVariable(Stack<ForthWord> forthStack, Robot robot) throws ForthRunTimeException {
        ForthWord first;
        ForthWord second;
        first = forthStack.pop();
        second = forthStack.pop();
        
        if(first instanceof ForthPointerLiteral){
            ((ForthPointerLiteral) first).setVariableValue(robot, second);
        } else {
            throw new ForthRunTimeException("! word called without a variable pointer on top of the stack");
        }
    }

    /**
     * false if either boolean is false, true otherwise
     *  ( b b -- b )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void and(Stack<ForthWord> forthStack) throws ForthRunTimeException {
        ForthWord first;
        ForthWord second;
        ForthWord result;
            first = forthStack.pop();
            second = forthStack.pop();
            if(first instanceof ForthBoolLiteral && second instanceof ForthBoolLiteral){
                boolean firstBool = ((ForthBoolLiteral) first).getValue();
                boolean secondBool = ((ForthBoolLiteral) second).getValue();
                result = new ForthBoolLiteral(firstBool && secondBool);
                forthStack.push(result);
            } else {
                throw new ForthRunTimeException("AND word called without two booleans on top of the stack");
            }
    }

    /**
     * true if either boolean is true, false otherwise
     *  ( b b -- b )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void or(Stack<ForthWord> forthStack) throws ForthRunTimeException {
        ForthWord first;
        ForthWord second;
        ForthWord result;
        first = forthStack.pop();
        second = forthStack.pop();
        if(first instanceof ForthBoolLiteral && second instanceof ForthBoolLiteral){
            boolean firstBool = ((ForthBoolLiteral) first).getValue();
            boolean secondBool = ((ForthBoolLiteral) second).getValue();
            result = new ForthBoolLiteral(firstBool || secondBool);
            forthStack.push(result);
        } else {
            throw new ForthRunTimeException("OR word called without two booleans on top of the stack");
        }
    }

    /**
     * invert the given boolean
     *  ( b -- b )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void invert(Stack<ForthWord> forthStack) throws ForthRunTimeException {
        ForthWord first;
        ForthWord result;
            first = forthStack.pop();
            if(first instanceof ForthBoolLiteral){
                boolean firstBool = ((ForthBoolLiteral) first).getValue();
                result = new ForthBoolLiteral(!firstBool);
                forthStack.push(result);
            } else {
                throw new ForthRunTimeException("invert word called without a boolean on top of the stack");
            }
    }

    /**
     * i2 is less than i1
     *  ( i2 i1 -- b )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void less(Stack<ForthWord> forthStack) throws ForthRunTimeException {
        ForthWord first;
        ForthWord second;
        ForthWord result;
        first = forthStack.pop();
        second = forthStack.pop();
        if(first instanceof ForthIntegerLiteral && second instanceof ForthIntegerLiteral){
            long firstInt = ((ForthIntegerLiteral) first).getValue();
            long secondInt = ((ForthIntegerLiteral) second).getValue();
            result = new ForthBoolLiteral(secondInt < firstInt);
            forthStack.push(result);
        } else {
            throw new ForthRunTimeException("< word called without two ints on top of the stack");
        }
    }

    /**
     * i2 is not more than i1
     *  ( i2 i1 -- b )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void lessOrEqual(Stack<ForthWord> forthStack) throws ForthRunTimeException {
        ForthWord first;
        ForthWord second;
        ForthWord result;
            first = forthStack.pop();
            second = forthStack.pop();
            if(first instanceof ForthIntegerLiteral && second instanceof ForthIntegerLiteral){
                long firstInt = ((ForthIntegerLiteral) first).getValue();
                long secondInt = ((ForthIntegerLiteral) second).getValue();
                result = new ForthBoolLiteral(secondInt <= firstInt);
                forthStack.push(result);
            } else {
                throw new ForthRunTimeException("<= word called without two ints on top of the stack");
            }
    }

    /**
     * i2 is equal to i1
     *  ( i2 i1 -- b )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void equal(Stack<ForthWord> forthStack) throws ForthRunTimeException {
        ForthWord first;
        ForthWord second;
        ForthWord result;
        first = forthStack.pop();
        second = forthStack.pop();
        if(first instanceof ForthIntegerLiteral && second instanceof ForthIntegerLiteral){
            long firstInt = ((ForthIntegerLiteral) first).getValue();
            long secondInt = ((ForthIntegerLiteral) second).getValue();
            result = new ForthBoolLiteral(secondInt == firstInt);
            forthStack.push(result);
        } else {
            throw new ForthRunTimeException("= word called without two ints on top of the stack");
        }
    }

    /**
     * i2 is not equal to i1
     *  ( i2 i1 -- b )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void notEqual(Stack<ForthWord> forthStack) throws ForthRunTimeException {
        ForthWord first;
        ForthWord second;
        ForthWord result;
        first = forthStack.pop();
        second = forthStack.pop();
        if(first instanceof ForthIntegerLiteral && second instanceof ForthIntegerLiteral){
            long firstInt = ((ForthIntegerLiteral) first).getValue();
            long secondInt = ((ForthIntegerLiteral) second).getValue();
            result = new ForthBoolLiteral(secondInt != firstInt);
            forthStack.push(result);
        } else {
            throw new ForthRunTimeException("<> word called without two ints on top of the stack");
        }
    }

    /**
     * i2 is at least i1
     *  ( i2 i1 -- b )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void greaterOrEqual(Stack<ForthWord> forthStack) throws ForthRunTimeException {
        ForthWord first;
        ForthWord second;
        ForthWord result;
            first = forthStack.pop();
            second = forthStack.pop();
            if(first instanceof ForthIntegerLiteral && second instanceof ForthIntegerLiteral){
                long firstInt = ((ForthIntegerLiteral) first).getValue();
                long secondInt = ((ForthIntegerLiteral) second).getValue();
                result = new ForthBoolLiteral(secondInt >= firstInt);
                forthStack.push(result);
            } else {
                throw new ForthRunTimeException(">= word called without two ints on top of the stack");
            }
    }

    /**
     * i2 is more than i1
     *  ( i2 i1 -- b )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void greater(Stack<ForthWord> forthStack) throws ForthRunTimeException {
        ForthWord first;
        ForthWord second;
        ForthWord result;
        first = forthStack.pop();
        second = forthStack.pop();
        if(first instanceof ForthIntegerLiteral && second instanceof ForthIntegerLiteral){
            long firstInt = ((ForthIntegerLiteral) first).getValue();
            long secondInt = ((ForthIntegerLiteral) second).getValue();
            result = new ForthBoolLiteral(secondInt > firstInt);
            forthStack.push(result);
        } else {
            throw new ForthRunTimeException("> word called without two ints on top of the stack");
        }
    }

    /**
     * add the two integers, pushing their sum on the stack
     * (i2 i1 -- i)
     * 
     * @param forthStack     the stack for the currently running forth program
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void add(Stack<ForthWord> forthStack) throws ForthRunTimeException {
        ForthWord first;
        ForthWord second;
        ForthWord result;
            first = forthStack.pop();
            second = forthStack.pop();
            if(first instanceof ForthIntegerLiteral && second instanceof ForthIntegerLiteral){
                long firstInt = ((ForthIntegerLiteral) first).getValue();
                long secondInt = ((ForthIntegerLiteral) second).getValue();
                result = new ForthIntegerLiteral(secondInt + firstInt);
                forthStack.push(result);
            } else {
                throw new ForthRunTimeException("+ word called without two ints on top of the stack");
            }
    }

    /**
     * subtract the top integer from the next, pushing their difference on the stack
     * ( i2 i1 -- i )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void subtract(Stack<ForthWord> forthStack) throws ForthRunTimeException {
        ForthWord first;
        ForthWord second;
        ForthWord result;
            first = forthStack.pop();
            second = forthStack.pop();
            if(first instanceof ForthIntegerLiteral && second instanceof ForthIntegerLiteral){
                long firstInt = ((ForthIntegerLiteral) first).getValue();
                long secondInt = ((ForthIntegerLiteral) second).getValue();
                result = new ForthIntegerLiteral(secondInt - firstInt);
                forthStack.push(result);
            } else {
                throw new ForthRunTimeException("- word called without two ints on top of the stack");
            }
    }

    /**
     * multiply the two top integers, pushing their product on the stack
     * ( i i -- i )
     * 
     * @param forthStack     the stack for the currently running forth program
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void multiply(Stack<ForthWord> forthStack) throws ForthRunTimeException {
        ForthWord first;
        ForthWord second;
        ForthWord result;
        first = forthStack.pop();
        second = forthStack.pop();
        if(first instanceof ForthIntegerLiteral && second instanceof ForthIntegerLiteral){
            long firstInt = ((ForthIntegerLiteral) first).getValue();
            long secondInt = ((ForthIntegerLiteral) second).getValue();
            result = new ForthIntegerLiteral(secondInt * firstInt);
            forthStack.push(result);
        } else {
            throw new ForthRunTimeException("* word called without two ints on top of the stack");
        }
    }

    /**
     * divide the top integer into the next, pushing the remainder and quotient
     *  ( iv ie -- iq ir)
     * 
     * @param forthStack     the stack for the currently running forth program
     * @throws ForthRunTimeException     thrown if an expected value is found that leaves the program unrunnable
     */
    protected static void divide(Stack<ForthWord> forthStack) throws ForthRunTimeException {
        ForthWord first;
        ForthWord second;
            first = forthStack.pop();
            second = forthStack.pop();
            if(first instanceof ForthIntegerLiteral && second instanceof ForthIntegerLiteral){
                long firstInt = ((ForthIntegerLiteral) first).getValue();
                long secondInt = ((ForthIntegerLiteral) second).getValue();
                ForthWord iq = new ForthIntegerLiteral(secondInt / firstInt);
                ForthWord ir = new ForthIntegerLiteral(secondInt % firstInt);
                forthStack.push(ir);
                forthStack.push(iq);
            } else {
                throw new ForthRunTimeException("/mod word called without two ints on top of the stack");
            }
    }


    /**
     * remove the value at the top of the stack
     * ( v -- )
     * 
     * @param forthStack     the stack for the currently running forth program
     */
    protected static void drop(Stack<ForthWord> forthStack) {
        forthStack.pop();
    }


    /**
     * duplicate the value at the top of the stack
     * ( v -- v v )
     * 
     * @param forthStack     the stack for the currently running forth program
     */
    protected static void duplicate(Stack<ForthWord> forthStack) {
        ForthWord first;
        first = forthStack.pop();
        forthStack.push(first);
        forthStack.push(first);
    }

    /**
     * rotate first three items on stack
     *  ( v3 v2 v1 -- v3 v1 v2 ) 
     *  @param forthStack     the stack for the currently running forth program
     **/
    protected static void rotate(Stack<ForthWord> forthStack) {
        ForthWord first;
        ForthWord second;
        ForthWord third;
            first = forthStack.pop();
            second = forthStack.pop();
            third = forthStack.pop();
            forthStack.push(second);
            forthStack.push(first);
            forthStack.push(third);
    }

    /**swap the two values at the top of the stack
     * ( v2 v1 -- v2 v1 )
     * @param forthStack     the stack for the currently running forth program
    **/
    protected static void swap(Stack<ForthWord> forthStack) {
        ForthWord first;
        ForthWord second;
        first = forthStack.pop();
        second = forthStack.pop();
        forthStack.push(first);
        forthStack.push(second);
    }
    
}