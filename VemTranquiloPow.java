package robots;
import java.awt.Color;
import java.awt.geom.*;
import robocode.util.Utils;
import robocode.*;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

public class VemTranquiloPow extends AdvancedRobot {
	double energiaInimigo = 100, anguloPosicionamento = 0;
  	int direcao = 1;
  	double anguloInimigoAnterior = 0;
  	
	public void run() {
		setColors(Color.black,Color.white,Color.white,Color.white,Color.white); // body,gun,radar,bullet,scan
		setAdjustGunForRobotTurn(false);
		setAdjustRadarForGunTurn(false);
		setAdjustRadarForRobotTurn(false);
		setMaxVelocity(Rules.MAX_VELOCITY);
		turnRadarRight(Double.POSITIVE_INFINITY);
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		// posiciona o radar para ultima posição do inimigo
		setTurnRadarLeft(getRadarTurnRemaining());
		// posicionando tanque em relação ao inimigo
		anguloPosicionamento = e.getBearing() + 90 - 30 * direcao;
		setTurnRight(anguloPosicionamento);
		atirar(e);
		// esquivas
		esquivarTiro(e);
		esquivarInimigo(e);
   		// atualizando energia do inimigo
		energiaInimigo = e.getEnergy();
	}
	
	private void esquivarTiro(ScannedRobotEvent e) {
		// variação de energia do inimigo
		double deltaEnergia = energiaInimigo-e.getEnergy();
		// desvia se um tiro foi disparado
		if (deltaEnergia>0 && deltaEnergia<=3) {
			direcao = -direcao;
		    //setAhead((e.getDistance()/4+50)*direcao);
			setAhead(100 * direcao);
		}
	}
	
	private void esquivarInimigo(ScannedRobotEvent e) {
		// distancia limite do inimigo
		if(e.getDistance() < 150 ){
			// determinando angulo de ataque do inimigo
			if(Math.round(e.getBearing()) >= -60) 
				setBack(75);
			else
				setAhead(75);	
		}
	}
	
	public void atirar(ScannedRobotEvent e) {
		double tiro = Rules.MAX_BULLET_POWER / Math.round(e.getDistance() / 200);	
		double anguloRelativo = getHeadingRadians() + e.getBearingRadians();
		double predicaoX = getX() + e.getDistance() * Math.sin(anguloRelativo);
		double predicaoY = getY() + e.getDistance() * Math.cos(anguloRelativo);
		double anguloInimigo = e.getHeadingRadians();
		double deltaAnguloInimigo = anguloInimigo - anguloInimigoAnterior;
		double velovidadeInimigo = e.getVelocity();
		double tempo = 0;	
		anguloInimigoAnterior = anguloInimigo;
		
		while((++tempo) * Rules.getBulletSpeed(tiro) < Point2D.Double.distance(getX(), getY(), predicaoX, predicaoY)) {		
			predicaoX += Math.sin(anguloInimigo) * velovidadeInimigo;
			predicaoY += Math.cos(anguloInimigo) * velovidadeInimigo;
			anguloInimigo += deltaAnguloInimigo;
		}
		
		double theta = Utils.normalAbsoluteAngle(Math.atan2(predicaoX - getX(), predicaoY - getY()));
		setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));
		if (predicaoX < getBattleFieldWidth() & predicaoX > 0 &
				predicaoY < getBattleFieldHeight() & predicaoY > 0) {
			setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));
			fire(tiro);
		}
	}
	
	public void onHitRobot(HitRobotEvent e) {
		fire(3);
	}
}