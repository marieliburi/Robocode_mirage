package FM;

import robocode.*;
import java.awt.Color;
import java.util.Random;

public class FM_Mirage extends AdvancedRobot {

    private Random aleatorio = new Random();  
    private boolean movendoParaFrente = true;  // Variável para rastrear o movimento (frente ou trás)
    private long ultimoTempoDeTiro = 0;  // Variável para armazenar o tempo do último disparo
    private boolean seguindoInimigo = false;  // Controle para seguir o inimigo
    private boolean sobAtaque = false;  // Controle para saber se o robô está sendo atacado
    private boolean emBusca = true;  // Controle para saber se o robô está em modo de busca

    //Testando bot do telegram
    
    public void run() {
        // Define as cores do robô
        setColors(new Color(128, 0, 128), Color.black, Color.blue);

        // Configura o radar e o canhão para girar independentemente do corpo do robô
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);

        // Define um movimento inicial
        iniciarMovimentoInicial();

        // Loop principal do robô
        while (true) {
            if (seguindoInimigo) {
                // Se o robô está seguindo um inimigo, gira o radar de forma contínua e mova-se
                setTurnRadarRight(360);  // Gira o radar continuamente
                if (!sobAtaque) {
                    movimentarProativamente();  // Movimenta o robô proativamente
                }
            } else if (emBusca) {
                // Se não está seguindo um inimigo e está em modo de busca, realiza movimento exploratório
                movimentoExploratorio();
            }
            execute();  // Executa ações pendentes
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent evento) {
        double distancia = evento.getDistance();  // Obtém a distância até o robô inimigo
        double potenciaDeTiro = Math.min(3, Math.max(1, 400 / distancia));  // Ajusta a potência do tiro com base na distância

        // Previsão da posição futura do inimigo
        double posXInimigo = getX() + distancia * Math.sin(Math.toRadians(getHeading() + evento.getBearing()));
        double posYInimigo = getY() + distancia * Math.cos(Math.toRadians(getHeading() + evento.getBearing()));
        double rumoInimigo = evento.getHeading();
        double velocidadeInimigo = evento.getVelocity();

        // Calcula a posição futura do inimigo
        double posXPrevista = posXInimigo + Math.sin(Math.toRadians(rumoInimigo)) * velocidadeInimigo;
        double posYPrevista = posYInimigo + Math.cos(Math.toRadians(rumoInimigo)) * velocidadeInimigo;

        // Calcula o ângulo para atirar no ponto previsto
        double anguloParaInimigo = Math.toDegrees(Math.atan2(posXPrevista - getX(), posYPrevista - getY()));
        double anguloDeGiroCanhao = normalizeBearing(anguloParaInimigo - getGunHeading());

        // Move o canhão para o ângulo calculado e atira
        turnGunRight(anguloDeGiroCanhao);
        fire(potenciaDeTiro);

        ultimoTempoDeTiro = getTime();  // Atualiza o tempo do último disparo
        seguindoInimigo = true;  // Inicia o seguimento do inimigo
        emBusca = false;  // Para o movimento de busca quando um inimigo é detectado
        seguirInimigo(evento);  // Atualiza o radar para seguir o inimigo

        // Se o robô não estiver sob ataque, execute movimento evasivo
        if (!sobAtaque) {
            movimentarProativamente();
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent evento) {
        sobAtaque = true;  // Marca que o robô está sob ataque

        double angulo = evento.getBearing();  // Obtém o ângulo da bala em relação ao robô
        double anguloEvasivo = (angulo > 0) ? 90 : -90;  // Mova perpendicularmente para a esquerda ou direita

        // Mova-se para uma distância razoável para fora da linha de tiro
        setTurnRight(anguloEvasivo);
        setAhead(150);  // Move-se para frente para aumentar a distância do inimigo

        execute();  // Executa o movimento
    }

    @Override
    public void onHitWall(HitWallEvent evento) {
        // Recua e gira para evitar continuar colidindo com a parede
        back(100);
        turnRight(130);
        ahead(50);
    }

    // Método para movimentar-se proativamente
    private void movimentarProativamente() {
        // Movimento aleatório para dificultar a previsão do inimigo
        setAhead(100 + aleatorio.nextInt(200));  // Move-se para frente de forma aleatória
        setTurnRight(30 + aleatorio.nextInt(60));  // Gira em um ângulo aleatório
        execute();  // Executa o movimento

        sobAtaque = false;  // Após o movimento evasivo, considera que o robô não está mais sob ataque
    }

    // Método para iniciar o movimento inicial
    private void iniciarMovimentoInicial() {
        // Realiza um movimento inicial padrão para explorar a arena
        setAhead(100 + aleatorio.nextInt(200));  // Move-se para frente de forma aleatória
        setTurnRight(90 + aleatorio.nextInt(180));  // Gira em um ângulo aleatório
        execute();  // Executa o movimento
        emBusca = true;  // Define que o robô está em modo de busca
    }

    // Método para realizar movimento exploratório quando não está seguindo um inimigo
    private void movimentoExploratorio() {
        // Realiza um movimento padrão para explorar a arena
        setAhead(100 + aleatorio.nextInt(200));  // Move-se para frente de forma aleatória
        setTurnRight(30 + aleatorio.nextInt(60));  // Gira em um ângulo aleatório
        turnRadarRight(360);  // Gira o radar para procurar novos inimigos
        execute();  // Executa o movimento
    }

    // Método para seguir o inimigo
    private void seguirInimigo(ScannedRobotEvent evento) {
        double giroRadar = getHeading() + evento.getBearing() - getRadarHeading();
        setTurnRadarRight(normalizeBearing(giroRadar));
    }

    // Método utilitário para normalizar o ângulo (entre -180 e 180 graus)
    private double normalizeBearing(double angulo) {
        while (angulo > 180) angulo -= 360;
        while (angulo < -180) angulo += 360;
        return angulo;
    }
}
