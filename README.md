# FlowPay

Esse foi o código da entrevista técnica.

Para subir todo o ambiente,
será necessário que seja executado no terminal os seguintes comandos:


```
npm i
npm run start
```

O projeto é uma fila de atendimentos onde há um atendimento por vez. O cliente entra na fila e aguarda o atendimento. O atendente pode ver a fila e atender o cliente. O atendimento é encerrado quando o atendente finaliza o atendimento ou o cliente o cancela.

A principal tecnologia utilizada é o Spring Boot para o backend e o Angular para o frontend.

O projeto foi desenvolvido em 1 dia e meio e conta com as seguintes funcionalidades:

- Cadastro de clientes
- Cadastro de atendimentos
- Fila de atendimentos
- Dashboard com métricas

As rotas acessíveis para o cliente são:

- / - Landing page
- /queue - Fila de atendimentos

As rotas acessíveis para o atendente são:

- /dashboard - Dashboard com métricas
- /attendant/${tipo} - Fila de atendimentos


Onde ${tipo} pode ser:
- CARD_PROBLEMS
- LOANS
- OTHER

O codigo do backend esta totalmente documentado com Javadocs, entao sinta-se a vontade para ler a documentacao para entender melhor o codigo.