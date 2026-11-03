package br.com.aulaSelenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutomationExerciseTest {

    private static final String URL = "https://automationexercise.com/";

    private static final By LOGIN_EMAIL = By.cssSelector("input[data-qa='login-email']");
    private static final By LOGIN_PASSWORD = By.cssSelector("input[data-qa='login-password']");
    private static final By CONTINUE_BUTTON = By.cssSelector("a[data-qa='continue-button']");

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.manage().window().maximize();
        driver.get(URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("a[href='/login']")
        ));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Testa cinco combinações de entradas inválidas no formulário de login.
     */
    @Test
    @DisplayName("Login com e-mail ou senha incorretos")
    void testLoginWithIncorrectEmailAndPassword() {
        openLoginPage();

        // CT01: e-mail sintaticamente válido, mas não cadastrado.
        attemptLogin("usuario.inexistente." + UUID.randomUUID() + "@gmail.com", "Senha123");

        WebElement errorMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[contains(., 'Your email or password is incorrect!')]")
                )
        );
        assertTrue(errorMessage.isDisplayed(),
                "A mensagem de credenciais incorretas deveria estar visível.");

        // CT02: e-mail em formato inválido.
        attemptLogin("email-invalido", "Senha123");
        assertFalse(isFieldValid(LOGIN_EMAIL),
                "O navegador deveria rejeitar o formato do e-mail.");

        // CT03: e-mail vazio.
        attemptLogin("", "Senha123");
        assertFalse(isFieldValid(LOGIN_EMAIL),
                "O campo e-mail não deveria aceitar valor vazio.");

        // CT04: senha vazia.
        attemptLogin("usuario.inexistente@gmail.com", "");
        assertFalse(isFieldValid(LOGIN_PASSWORD),
                "O campo senha não deveria aceitar valor vazio.");

        // CT05: e-mail e senha vazios.
        attemptLogin("", "");
        assertFalse(isFieldValid(LOGIN_EMAIL),
                "O campo e-mail deveria ser obrigatório.");
        assertFalse(isFieldValid(LOGIN_PASSWORD),
                "O campo senha deveria ser obrigatório.");
    }

    /**
     * Cadastra um usuário novo, verifica o login e exclui a conta criada.
     */
    @Test
    @DisplayName("Registrar novo usuário")
    void testRegisterUser() {
        openLoginPage();

        String username = "UsuarioTeste";
        String email = "teste" + UUID.randomUUID().toString().replace("-", "") + "@gmail.com";

        driver.findElement(By.cssSelector("input[data-qa='signup-name']")).sendKeys(username);
        driver.findElement(By.cssSelector("input[data-qa='signup-email']")).sendKeys(email);
        driver.findElement(By.cssSelector("button[data-qa='signup-button']")).click();

        WebElement accountInformation = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//b[normalize-space()='Enter Account Information']")
                )
        );
        assertTrue(accountInformation.isDisplayed());

        // Informações da conta.
        driver.findElement(By.id("id_gender1")).click();
        driver.findElement(By.id("password")).sendKeys("Senha123");

        new Select(driver.findElement(By.id("days"))).selectByValue("15");
        new Select(driver.findElement(By.id("months"))).selectByValue("6");
        new Select(driver.findElement(By.id("years"))).selectByValue("2000");

        // Marca as opções somente se ainda estiverem desmarcadas.
        selectCheckbox(By.id("newsletter"));
        selectCheckbox(By.id("optin"));

        // Endereço e informações pessoais.
        driver.findElement(By.id("first_name")).sendKeys("V");
        driver.findElement(By.id("last_name")).sendKeys("Fonseca");
        driver.findElement(By.id("company")).sendKeys("Empresa Teste");
        driver.findElement(By.id("address1")).sendKeys("Rua Teste, 123");
        driver.findElement(By.id("address2")).sendKeys("Apartamento 101");
        new Select(driver.findElement(By.id("country"))).selectByVisibleText("United States");
        driver.findElement(By.id("state")).sendKeys("California");
        driver.findElement(By.id("city")).sendKeys("Los Angeles");
        driver.findElement(By.id("zipcode")).sendKeys("90001");
        driver.findElement(By.id("mobile_number")).sendKeys("21999999999");

        driver.findElement(By.cssSelector("button[data-qa='create-account']")).click();

        WebElement accountCreated = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("h2[data-qa='account-created']")
                )
        );
        assertTrue(accountCreated.isDisplayed(),
                "A mensagem ACCOUNT CREATED! deveria estar visível.");

        closeGoogleVignetteIfPresent();
        clickContinueButton();
        closeGoogleVignetteIfPresent();

        WebElement loggedUser = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//a[contains(normalize-space(.), 'Logged in as')]")
                )
        );
        assertTrue(loggedUser.getText().contains(username),
                "O nome do usuário deveria aparecer após o login.");

        driver.findElement(By.cssSelector("a[href='/delete_account']")).click();

        WebElement accountDeleted = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("h2[data-qa='account-deleted']")
                )
        );
        assertTrue(accountDeleted.isDisplayed(),
                "A mensagem ACCOUNT DELETED! deveria estar visível.");

        closeGoogleVignetteIfPresent();
        clickContinueButton();
    }

    /** Abre a página de login/cadastro. */
    private void openLoginPage() {
        driver.findElement(By.cssSelector("a[href='/login']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(., 'Login to your account')]")
        ));
    }

    /** Preenche os campos e tenta realizar o login. */
    private void attemptLogin(String email, String password) {
        WebElement emailField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(LOGIN_EMAIL)
        );
        WebElement passwordField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(LOGIN_PASSWORD)
        );

        emailField.clear();
        emailField.sendKeys(email);
        passwordField.clear();
        passwordField.sendKeys(password);

        driver.findElement(By.cssSelector("button[data-qa='login-button']")).click();
    }

    /**
     * Consulta a validade HTML5 de um campo sem realizar unboxing de Boolean nulo.
     */
    private boolean isFieldValid(By locator) {
        WebElement field = wait.until(
                ExpectedConditions.visibilityOfElementLocated(locator)
        );

        Object result = ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].checkValidity();",
                field
        );

        assertNotNull(result, "checkValidity() não retornou um resultado.");
        assertTrue(result instanceof Boolean,
                "checkValidity() deveria retornar um valor booleano.");

        // Boolean.TRUE.equals evita converter (unbox) um Boolean possivelmente nulo.
        return Boolean.TRUE.equals(result);
    }

    /** Marca um checkbox apenas quando ele ainda não está selecionado. */
    private void selectCheckbox(By locator) {
        WebElement checkbox = wait.until(
                ExpectedConditions.elementToBeClickable(locator)
        );
        if (!checkbox.isSelected()) {
            checkbox.click();
        }
    }

    /**
     * Clica em Continue. Primeiro tenta o clique normal do Selenium;
     * se o Firefox não conseguir interagir com o elemento, usa JavaScript.
     */
    private void clickContinueButton() {
        WebElement button = wait.until(
                ExpectedConditions.presenceOfElementLocated(CONTINUE_BUTTON)
        );

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', inline: 'center'});",
                button
        );

        try {
            wait.until(ExpectedConditions.elementToBeClickable(CONTINUE_BUTTON)).click();
        } catch (ElementNotInteractableException e) {
            button = driver.findElement(CONTINUE_BUTTON);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
        }
    }

    /**
     * Tenta fechar o anúncio intersticial do Google, caso esteja presente.
     * O anúncio pode estar no documento principal ou em um iframe aswift_*. 
     */
    private void closeGoogleVignetteIfPresent() {
        driver.switchTo().defaultContent();

        // Em algumas renderizações, o controle Close fica no documento principal.
        List<WebElement> closeControls = driver.findElements(
                By.xpath("//*[self::a or self::button or @role='button'][normalize-space(.)='Close' or @aria-label='Close']")
        );
        for (WebElement control : closeControls) {
            try {
                if (control.isDisplayed() && control.isEnabled()) {
                    control.click();
                    driver.switchTo().defaultContent();
                    return;
                }
            } catch (ElementNotInteractableException | StaleElementReferenceException e) {
                // Continua tentando as outras possibilidades.
            }
        }

        driver.switchTo().defaultContent();
        List<WebElement> adFrames = driver.findElements(
                By.cssSelector("iframe[name^='aswift_']")
        );

        for (WebElement adFrame : adFrames) {
            try {
                driver.switchTo().defaultContent();
                driver.switchTo().frame(adFrame);

                List<WebElement> closeButtons = driver.findElements(By.id("dismiss-button"));
                if (!closeButtons.isEmpty()) {
                    WebElement closeButton = closeButtons.get(0);
                    if (closeButton.isDisplayed() && closeButton.isEnabled()) {
                        closeButton.click();
                        System.out.println("Anúncio Google fechado!");
                        return;
                    }
                }
            } catch (NoSuchFrameException | StaleElementReferenceException |
                     ElementNotInteractableException e) {
                // O anúncio pode ter sido removido ou atualizado durante a busca.
            } finally {
                driver.switchTo().defaultContent();
            }
        }
    }
}
