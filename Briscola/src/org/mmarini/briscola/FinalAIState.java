/**
 * 
 */
package org.mmarini.briscola;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Game state in case the player moves.
 * <p>
 * The game is in the final state where both the player knows the opposite
 * cards. The strategy can be completed defined.
 * </p>
 * 
 * @author US00852
 * 
 */
public class FinalAIState extends AbstractVirtualGameState {
	private static Logger logger = LoggerFactory.getLogger(FinalAIState.class);

	/**
	 * 
	 */
	public FinalAIState() {
	}

	/**
	 * 
	 * @param aiCard
	 * @param playerCard
	 * @return
	 */
	private AbstractGameState createState(Card aiCard, Card playerCard) {
		logger.debug("Creating state playing {}, {}", aiCard, playerCard);
		Card trump = getTrump();
		boolean winner = aiCard.wins(playerCard, trump);
		int score = computeScore(aiCard, playerCard);

		Card[] aiCards = createAndRemove(getAiCards(), aiCard);
		Card[] playerCards = createAndRemove(getPlayerCards(), playerCard);
		int playerScore = getPlayerScore();
		int aiScore = getAiScore();
		AbstractVirtualGameState state = null;
		if (winner) {
			aiScore += score;
			if (aiCards.length == 1) {
				state = new LastHandAIState();
			} else {
				state = new FinalAIState();
			}
		} else {
			playerScore += score;
			if (aiCards.length == 1) {
				state = new LastHandPlayerState();
			} else {
				state = new FinalPlayerStartState();
			}
		}
		state.setTrump(trump);
		state.setAiCards(aiCards);
		state.setPlayerCards(playerCards);
		state.setDeckCards();
		state.setAiScore(aiScore);
		state.setPlayerScore(playerScore);
		return state;
	}

	/**
	 * @see org.mmarini.briscola.AbstractGameState#estimate(org.mmarini.briscola.
	 *      Estimation, org.mmarini.briscola.StrategySearchContext)
	 */
	@Override
	public void estimate(Estimation estimation, StrategySearchContext ctx)
			throws InterruptedException {
		estimation.setConfident(true);
		estimation.setAiWinProb(0.);
		estimation.setPlayerWinProb(0.);
		int score = getAiScore();
		if (score > HALF_SCORE) {
			estimation.setAiWinProb(1.);
			return;
		}
		int oppositeScore = getPlayerScore();
		if (oppositeScore > HALF_SCORE) {
			estimation.setPlayerWinProb(1.);
			return;
		}

		Card[] oppositeCards = getPlayerCards();
		Card[] playerCards = getAiCards();
		Card bestCard = playerCards[0];
		double wp = Double.NEGATIVE_INFINITY;
		double lp = 0;
		for (Card card : playerCards) {
			double pl = Double.NEGATIVE_INFINITY;
			double pw = 0;
			for (Card opposite : oppositeCards) {
				AbstractGameState state = createState(card, opposite);
				state.estimate(estimation, ctx);
				double p = estimation.getPlayerWinProb();
				if (p > pl) {
					pl = p;
					pw = estimation.getAiWinProb();
				}
			}
			if (pw > wp) {
				wp = pw;
				lp = pl;
				bestCard = card;
			}
		}
		estimation.setAiWinProb(wp);
		estimation.setPlayerWinProb(lp);
		estimation.setConfident(true);
		estimation.setBestCard(bestCard);
	}
}
