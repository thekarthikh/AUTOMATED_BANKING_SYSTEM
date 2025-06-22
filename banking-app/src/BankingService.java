// BankingService.java - Business logic layer
import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

public class BankingService {
    private AccountDAO accountDAO;
    private TransactionDAO transactionDAO;
    
    public BankingService() {
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
    }
    
    public String createAccount(String accountHolderName, String email, BigDecimal initialDeposit, String accountType) {
        String accountNumber = generateAccountNumber();
        Account account = new Account(accountNumber, accountHolderName, email, initialDeposit, accountType);
        
        if (accountDAO.createAccount(account)) {
            return accountNumber;
        }
        return null;
    }
    
    public boolean deposit(String accountNumber, BigDecimal amount, String description) {
        Account account = accountDAO.getAccountByNumber(accountNumber);
        if (account == null) return false;
        
        BigDecimal newBalance = account.getBalance().add(amount);
        
        if (accountDAO.updateBalance(accountNumber, newBalance)) {
            Transaction transaction = new Transaction(null, accountNumber, amount, "DEPOSIT", description);
            transactionDAO.createTransaction(transaction);
            return true;
        }
        return false;
    }
    
    public boolean withdraw(String accountNumber, BigDecimal amount, String description) {
        Account account = accountDAO.getAccountByNumber(accountNumber);
        if (account == null || account.getBalance().compareTo(amount) < 0) return false;
        
        BigDecimal newBalance = account.getBalance().subtract(amount);
        
        if (accountDAO.updateBalance(accountNumber, newBalance)) {
            Transaction transaction = new Transaction(accountNumber, null, amount, "WITHDRAWAL", description);
            transactionDAO.createTransaction(transaction);
            return true;
        }
        return false;
    }
    
    public boolean transfer(String fromAccount, String toAccount, BigDecimal amount, String description) {
        Account from = accountDAO.getAccountByNumber(fromAccount);
        Account to = accountDAO.getAccountByNumber(toAccount);
        
        if (from == null || to == null || from.getBalance().compareTo(amount) < 0) return false;
        
        BigDecimal fromNewBalance = from.getBalance().subtract(amount);
        BigDecimal toNewBalance = to.getBalance().add(amount);
        
        if (accountDAO.updateBalance(fromAccount, fromNewBalance) && 
            accountDAO.updateBalance(toAccount, toNewBalance)) {
            Transaction transaction = new Transaction(fromAccount, toAccount, amount, "TRANSFER", description);
            transactionDAO.createTransaction(transaction);
            return true;
        }
        return false;
    }
    
    public BigDecimal getBalance(String accountNumber) {
        Account account = accountDAO.getAccountByNumber(accountNumber);
        return account != null ? account.getBalance() : BigDecimal.ZERO;
    }
    
    public List<Transaction> getTransactionHistory(String accountNumber) {
        return transactionDAO.getTransactionsByAccount(accountNumber);
    }
    
    public List<Account> getAllAccounts() {
        return accountDAO.getAllAccounts();
    }
    
    private String generateAccountNumber() {
        Random random = new Random();
        return "ACC" + (100000000 + random.nextInt(900000000));
    }
}