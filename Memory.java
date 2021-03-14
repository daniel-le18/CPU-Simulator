import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

/* Memory will read an input file containing a program into its array, before any CPU fetching begins.
!Note that the memory is simply storage; it has no real logic beyond reading and writing. 
!It will support two operations:
       ?read(address) -  returns the value at the address
       ?write(address, data) - writes the data to the address
*/

public class Memory {
    final static int[] memory = new int[2000];

    public static void main(String[] args) {
        Memory memory = new Memory();
        try {
            Scanner cpu_reader = new Scanner(System.in);

            String file_name = null;
            if (cpu_reader.hasNext())
                file_name = cpu_reader.nextLine();
            memory.populate_MEM(file_name);

            String line;
            int address;
            int value;
            char mode;

            while (true) {
                if (cpu_reader.hasNext()) {
                    line = cpu_reader.nextLine();
                    mode = line.charAt(0);

                    if (mode == 'r') {
                        address = Integer.parseInt(line.substring(1));
                        System.out.println(memory.read(address));
                    }
                    else if (mode == 'w') {
                        int position = line.indexOf(" ");
                        address = Integer.parseInt(line.substring(1, position));
                        value = Integer.parseInt(line.substring(position + 1));
                        memory.write(address, value);
                    }
                } else
                    break;
            }

            cpu_reader.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void populate_MEM(String filename) {
        try {
            int address = 0;
            String line;
            // Read file
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("File not found !");
            }
            Scanner fileReader = new Scanner(file);

            // Read through file and put into array
            while (fileReader.hasNext()) {
                if (fileReader.hasNextInt()) {
                    int number = fileReader.nextInt();
                    memory[address] = number;
//                     System.out.println("["+address+"]"+" "+memory[address]);
                    address++;
                } else {
                    line = fileReader.next();
                    // If line start with .address, cut"." and set the next int at that address
                    if (line.charAt(0) == '.') {
                        address = Integer.parseInt(line.substring(1));
                        // If comment skip
                    } else if (line.equals("//")) {
                        fileReader.nextLine();
                        // blank line
                    } else {
                        fileReader.nextLine();
                    }
                }
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Getter
    public int read(int address) {
        return memory[address];
    }

    // Setter
    public void write(int address, int value) {
        memory[address] = value;
    }

}
