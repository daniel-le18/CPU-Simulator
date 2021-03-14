import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class CPU {
    // example1
    static int PC = 0;
    static int IR = 0;
    static int AC = 0;
    static int x = 0;
    static int y = 0;

    // example2
    static int SP = 1000;

    static int user_Stack = 1000;
    static int system_Stack = 2000;

    // example 3
    static int number_of_instructions = 0;
    static int timer = 0;

    // kernel mode is set to false if on interrupt
    static boolean  userMode = true;

    // flag that interrupt is in process
    static boolean isInterrupting = false;

    public static void main(String[] args) {

        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("java Memory");

            InputStream is = proc.getInputStream();
            OutputStream os = proc.getOutputStream();

            /* Write to memory */
            PrintWriter memory_writer = new PrintWriter(os);

            String file_name = args[0];
            timer = Integer.parseInt(args[1]);


            if (timer <0 || args == null) {
                System.out.println("Invalid input");
                System.exit(0);
            }


            memory_writer.println(file_name);
            memory_writer.flush();

            /* Read from memory */
            Scanner memory_reader = new Scanner(is);

            do {
                if (!isInterrupting && (number_of_instructions % timer == 0) && number_of_instructions>0) {

                    // Interrupting
                    activate_interrupt(memory_writer);
                }
                IR = read_Memory(memory_writer, memory_reader, PC);
                execute(IR, memory_writer, memory_reader);

            } while (IR != -1);

            proc.waitFor();
            int exitVal = proc.exitValue();
            System.out.println("Process exited: " + exitVal);
            memory_reader.close();

        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("Something wrong");
            System.exit(0);
        }
    }

    private static void execute(int IR, PrintWriter memory_writer, Scanner memory_reader) {

        int value;
        if (!isInterrupting) {
            number_of_instructions++;
        }
        switch (IR) {
        /* Case 1: Load the value into the AC */
        case 1:
            PC++;
            value = read_Memory(memory_writer, memory_reader, PC);
            AC = value;
            PC++;
 
            break;

        /* Case 2: Load the value at address into the AC */
        case 2:
            PC++;
            value = read_Memory(memory_writer, memory_reader, PC);
            AC = read_Memory(memory_writer, memory_reader, value);

            PC++;
            // System.out.println("AC: "+AC);

 
            break;

        /*
         * Case 3:Load the value from the address found in the given address into the AC
         */
        case 3:
            PC++;
            value = read_Memory(memory_writer, memory_reader, PC);
            value = read_Memory(memory_writer, memory_reader, value);
            AC = read_Memory(memory_writer, memory_reader, value);
            PC++;

 
            break;

        /* Case 4: LoadInxX */
        case 4:
            PC++;
            value = read_Memory(memory_writer, memory_reader, PC);
            AC = read_Memory(memory_writer, memory_reader, value + x);
            PC++;

 
            break;

        /* Case 5: LoadInxY */
        case 5:
            PC++;
            value = read_Memory(memory_writer, memory_reader, PC);
            AC = read_Memory(memory_writer, memory_reader, value + y);
            PC++;
            break;

        /*
         * Case 6: Load from (Sp+X) into the AC (if SP is 990, and X is 1, load from
         * 991)
         */
        case 6:
            AC = read_Memory(memory_writer, memory_reader, SP + x);
            PC++;
            // System.out.println("AC:"+AC+'\n');

 
            break;

        /* Case 7: Store the value in the AC into the address) */
        case 7:
            PC++;
            value = read_Memory(memory_writer, memory_reader, PC);
            write_Memory(memory_writer, value, AC);
            PC++;

 
            break;

        /* Case 8: Get random number from 1-100 into AC */
        case 8:
            AC = (int) (Math.random() * 100 + 1);
            PC++;

 
            break;

        /* Case 9: Put port,if 1 print AC- 2 print number value */
        case 9:
            PC++;
            value = read_Memory(memory_writer, memory_reader, PC);

            if (value == 1) {
                System.out.print(AC);
            } else if (value == 2) {
                System.out.print((char) AC);
            } else {
                System.err.println("Port error !");
            }

            PC++;
            break;

        /* Case 10: Add value in x to the AC */
        case 10:
            AC = AC + x;
            PC++;
            break;

        /* Case 11: Add value in y to the AC */
        case 11:
            AC = AC + y;
            PC++;
            break;

        /* Case 12:Subtract the value in x from the AC */
        case 12:
            AC = AC - x;
            PC++;
            break;

        /* Case 13:Subtract the value in y from the AC */
        case 13:
            AC = AC - y;
            PC++;
            break;

        /* Case 14: Copy the value in AC to x */
        case 14:
            x = AC;
            // System.out.println("X :" + x + "\n"+"AC: " + AC);
            PC++;
            break;

        /* Case 15: Copy the value in x to AC */
        case 15:
            AC = x;
            PC++;
            break;

        /* Case 16: Copy the value in AC to y */
        case 16:
            y = AC;
            // System.out.println("Y :" + y + "\n"+"AC: " + AC);
            PC++;
            break;

        /* Case 17: Copy value in Y to AC */
        case 17:
            AC = y;
            PC++;
            break;

        /* Case 18: Copy AC to SP */
        case 18:
            SP = AC;
            PC++;
 
            break;

        /* Case 19: Copy SP to AC */
        case 19:
            AC = SP;
            PC++;
            break;

        /* Case 20: Jump to the address */
        case 20:
            PC++;
            value = read_Memory(memory_writer, memory_reader, PC);
            PC = value;
            // System.out.println("Jump to: " + PC + "\n");
            break;

        /* Case 21: Jump to the address only if the value in the AC is zero */
        case 21:
            PC++;
            value = read_Memory(memory_writer, memory_reader, PC);
            if (AC == 0) {
                PC = value;
                // System.out.println("Jumped\n");
                break;
            }
            PC++;
            break;

        /* Case 22: Jump to the address only if the value in the AC is NOT zero */
        case 22:
            PC++;
            value = read_Memory(memory_writer, memory_reader, PC);
            if (AC != 0) {
                PC = value;
                // System.out.println("Jumped\n");
                break;
            }
            PC++;
            break;

        /* Case 23: Push return address onto stack, jump to the address */
        case 23:
            PC++;
            value = read_Memory(memory_writer, memory_reader, PC);
            push_stack(memory_writer, PC + 1);
            user_Stack = SP;

            // Jump to address
            PC = value;
            break;

        /* Case 24: Pop return address from the stack, jump to the address */
        case 24:
            value = pop_stack(memory_writer, memory_reader);
            PC = value;
            break;

        /* Case 25: Increase the value in x */
        case 25:
            x++;
            PC++;
            // System.out.println(x + "\n");
            break;

        /* Case 26: Push AC onto stack */
        case 26:
            x--;
            PC++;
            break;

        /* Case 27: Push AC onto stack */
        case 27:
            push_stack(memory_writer, AC);
            PC++;
            break;

        /* Case 28: Pop stack into AC */
        case 28:
            AC = pop_stack(memory_writer, memory_reader);
            PC++;
            break;

        /* Case 29: Perform syscall */
        case 29:
            isInterrupting = true;
            userMode = false;
            value = SP;

            // The stack pointer should be switched to the system stack.
            SP = system_Stack;

            // The SP and PC registers (and only these registers) should be saved on the
            // system stack by the CPU.
            push_stack(memory_writer, value);
            value = PC + 1;
            PC = 1500;
            push_stack(memory_writer, value);

            // The int instruction should cause execution at address 1500.
 
            break;

        /* Case 30:Return from system call */
        case 30:
            PC = pop_stack(memory_writer, memory_reader);
            SP = pop_stack(memory_writer, memory_reader);
            // System.out.println(PC + " " + SP + '\n');
 
             userMode = true;
            number_of_instructions++;
            isInterrupting = false;
            break;

        /* END */
        case 50:
            System.out.println("Case 50: End ");
            System.exit(0);
            break;
        default:
            break;
        }

    }

    private static int read_Memory(PrintWriter memory_writer, Scanner memory_reader, int address) {
        // Check violation if address trying to access out of range
        if (address >=1000 &&  userMode){
            System.out.println("Memory violation: accessing system address 1000 in user mode ");
            System.exit(0);
        }
        // Send r-signal with address to memory
        memory_writer.println("r" + address);
        memory_writer.flush();

        if (memory_reader.hasNext()) {
            String instr = memory_reader.nextLine();
            return Integer.parseInt(instr);
        }else{
            return -1;
        }

    }

    private static void write_Memory(PrintWriter memory_writer, int address, int value) {
        memory_writer.println("w" + address + " " + value);
        memory_writer.flush();
    }

    private static int pop_stack(PrintWriter memory_writer, Scanner memory_reader) {
        int value = read_Memory(memory_writer, memory_reader, SP);
        write_Memory(memory_writer, SP, -1);
        SP++;
        return value;
    }

    private static void push_stack(PrintWriter memory_writer, int value) {
        SP--;
        write_Memory(memory_writer, SP, value);
    }

    /*
     * The handler may save additional registers. A timer interrupt should cause
     * execution at address 1000. The i-ret instruction returns from an interrupt.
     * Interrupts should be disabled during interrupt processing to avoid nested
     * execution.
     */
    private static void activate_interrupt(PrintWriter memory_writer) {
        isInterrupting = true;
        userMode = false;
        int value = SP;
        /* The stack pointer should be switched to the system stack. */
        SP = system_Stack;

        /*
         * The SP and PC registers (and only these registers) should be saved on the
         * system stack by the CPU.
         */
        push_stack(memory_writer, value);
        value = PC;
        PC = 1000;
        push_stack(memory_writer, value);
    }
}
