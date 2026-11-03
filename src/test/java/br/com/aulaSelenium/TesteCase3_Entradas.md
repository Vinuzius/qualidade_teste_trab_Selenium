| ID | E-mail | Senha | Resultado esperado |
| :--- | :--- | :--- | :--- |
| **CT01** | E-mail válido não cadastrado | Senha incorreta | Mensagem de erro do sistema |
| **CT02** | E-mail sem @ | Senha123 | Validação HTML5 do e-mail |
| **CT03** | Campo e-mail vazio | Senha123 | Campo obrigatório |
| **CT04** | E-mail válido | Campo senha vazio | Campo obrigatório |
| **CT05** | E-mail vazio | Senha vazia | Campos obrigatórios |

---

### Aplicação das técnicas:

* **Classes de equivalência:** e-mail válido, e-mail inválido, e-mail vazio e senha vazia.
* **Valores limite:** testar os campos no limite inferior de preenchimento, ou seja, com zero caracteres.