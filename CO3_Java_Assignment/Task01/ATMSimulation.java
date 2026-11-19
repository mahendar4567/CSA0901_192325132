import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.InputMismatchException;
import java.util.Scanner;

class InsufficientFundsException extends RuntimeException {
    InsufficientFundsException(String message) { super(message); }
}

class ATM {
    private double balance = 5000.0;

    void withdraw(int account, double amount) {
        if (account <= 0) throw new IllegalArgumentException("Invalid account number");
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (amount > balance) throw new InsufficientFundsException("Insufficient balance");
        balance -= amount;
        System.out.println("Withdrawal successful. Balance: " + balance);
    }
}

public class ATMSimulation {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ATM atm = new ATM();
        try {
            System.out.print("Enter account number: ");
            int account = sc.nextInt();
            System.out.print("Enter withdrawal amount: ");
            double amount = sc.nextDouble();
            try (FileInputStream card = new FileInputStream("card-reader.dat")) {
                System.out.println("Card reader file verified.");
                atm.withdraw(account, amount);
            } catch (FileNotFoundException e) {
                System.out.println("Card file error: " + e.getMessage());
            } catch (java.io.IOException e) {
                System.out.println("Card reader I/O error: " + e.getMessage());
            } catch (IllegalArgumentException | InsufficientFundsException e) {
                System.out.println("Transaction error: " + e.getMessage());
            }
        } catch (InputMismatchException e) {
            System.out.println("Input error: enter numeric values.");
        } finally {
            System.out.println("ATM transaction session ended.");
            sc.close();
        }
    }
}
