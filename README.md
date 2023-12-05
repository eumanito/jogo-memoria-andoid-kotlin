
# Jogo da Memória
versão 1.0 - 04/12/2023

# Descrição
Criar aplicativo para funcionar como um jogo da memória utilizando cartões com imagens de personagens do desenho animado Rick and Morty.
Trabalho desenvolvido na disciplina de programação de aplicativos móveis.

# Integrantes
* Jailson Maciel
* Gabriel da Silva Machado

# Etapa 1 - Autenticação
* Login com usuário e senha
* Cadastro de novo usuário
* Autenticação de 2 fatores

# Etapa 2 - Tela estática
* 1 card flip funcional no centro da tela
* 1 cronômetro no topo da tela - armazena tempo até acertar
* 1 campo pontuação - incrementa quando passa de fase

# Etapa 3 - Consumo da API
* Usando Retrofit, consumir api: [https://rickandmortyapi.com/](https://rickandmortyapi.com/)
* Utilizar método **Get a single character**
* Criar lógica randômica para carregar os caracteres (1 até 42)

# Etapa 4 - Montagem da tela do jogo
* Criar fase 1 - 3 personagens duplicados = 6 cards

# Etapa 5 - Criar lógica do jogo
* Ler lista de caracteres, embaralhar e montar cards duplicados

# Etapa 6 - Armazenar resultado do jogo
* Ao finalizar o jogo, armazenar o tempo decorrido no Firebase
