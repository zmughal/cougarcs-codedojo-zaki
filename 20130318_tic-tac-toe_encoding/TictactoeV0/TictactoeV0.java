/* Encode a game of tic-tac-toe
 *
 * To encode a single move, it is possible to just use bits for the position
 * and the player as shown in encode_move().
 *
 * This allows us to store the entire game history in 45 bits (i.e. a 64-bit
 * long).
 */
import static java.lang.System.*;
import java.util.Arrays;

public class TictactoeV0 {
	public static void main(String args[]) throws Exception {
		tests();
		Game g = new Game();
		is(g.draw_board(false), "---------", false); /* empty */

		g.make_a_move(Position.TL, Player.X);
		is(g.draw_board(false), "X--------", false);

		g.make_a_move(Position.MC, Player.O);
		is(g.draw_board(false), "X---O----", false);

		g.make_a_move(Position.BR, Player.X);
		is(g.draw_board(false), "X---O---X", false);

		g.make_a_move(Position.MR, Player.O);
		is(g.draw_board(false), "X---OO--X", false);

		System.out.println();
		System.out.println("Game sequence:");
		is(g.draw_board(true), "X--------\n"
				+ "X---O----\n"
				+ "X---O---X\n"
				+ "X---OO--X\n", true);
	}


	public static void is(Object got, Object expected, boolean toOut) throws Exception {
		if( !got.equals(expected) ) {
			throw new Exception("failed: got: " + got + ", expected: " + expected);
		}
		if( toOut ) {
			out.println(got);
		}
	}

	public static void is(int got, int expected, boolean toOut) throws Exception {
		if( got != expected ) {
			throw new Exception("failed: got: " + got + ", expected: " + expected);
		}
		if( toOut ) {
			out.println(got);
		}
	}

	public static void tests() throws Exception {
		is( Game.encode_move(Position.TL.ordinal(), Player.X.ordinal()),  0, false );
		is( Game.encode_move(Position.TL.ordinal(), Player.O.ordinal()), 16, false );
		is( Game.encode_move(Position.TC.ordinal(), Player.X.ordinal()),  1, false );
		is( Game.encode_move(Position.TC.ordinal(), Player.O.ordinal()), 17, false );
	}
}

class Game {
	static final byte MOVE_SZ = 5;
	static final byte MOVE_MASK = 0x1F; /* 1 1111 _ 2 */
	static final byte EMPTY_MOVE = 0x1F; /* 1 1111 _ 2 */
	public long EMPTY_GAME = Long.MAX_VALUE;
	public long game_state = EMPTY_GAME;

	String draw_board(boolean each_move) {
		String board_data = "";
		char[] board = new char[9];
		Arrays.fill(board, '-');
		int n_moves = next_move();
		for(int n = 0; n <= n_moves; n++) {
			byte move = get_nth_move(n);
			byte where = decode_move_where(move);
			if( where == -1 ) {
				continue;
			}
			byte who = decode_move_who(move);
			//board[where] = who == 0 ? 'X' : 'O'; /* equivalent to the following */
			board[where] = Player.values()[who].name().charAt(0);
			if(each_move) {
				board_data += new String(board) + "\n";
			}
		}
		if(!each_move) {
			return new String(board);
		}
		return board_data;
	}

	public void make_a_move(Position position, Player p) {
		/* build move */
		/* NOTE: this does not check to see if the position has already been taken */
		int where = position.ordinal();
		int who = p.ordinal();
		byte move = encode_move(where, who);

		int nth_move = next_move();
		//out.println("Next move: " + nth_move);

		place_move(move, nth_move);
	}

	/* add move to game state */
	private void place_move(byte move, int n) {
		long set_mask = MOVE_MASK << (n*MOVE_SZ);
		game_state = ( game_state & ~set_mask ) | ( move << (n*MOVE_SZ) );
	}

	/* get the move stored in position n */
	public byte get_nth_move(int n) {
		return (byte) ( (game_state >> (n*MOVE_SZ)) & MOVE_MASK );
	}

	/* finds the next move (the first move position that is empty)
	 *
	 * I could store this as an counter, but that would technically add to
	 * the space required for the state.
	 */
	private int next_move() {
		for(int n = 0; n < 9; n++) {
			if( get_nth_move(n) == EMPTY_MOVE) {
				return n;
			}
		}
		return 9;
	}

	public static byte encode_move(int where, int who) {
		/* layout: wpppp */
		/* w = who bit, pppp = where bits */
		return (byte) ( (who << MOVE_SZ - 1 ) | where );
	}
	private static byte decode_move_where(byte move) {
		byte where = (byte) ( move & 0xF );
		if(where > 8) {
			return -1;
		}
		return where;
	}
	private static byte decode_move_who(byte move) {
		return (byte) ( move >> MOVE_SZ - 1 );
	}
}

enum Player {
	X /* = 0 */, O /* = 1 */;
};

enum Position {
	/*
	 * 0|1|2
	 * -+-+-
	 * 3|4|5
	 * -+-+-
	 * 6|7|8
	 */
	/* L = left, C = center, R = right */
	TL, TC, TR, /* top */
	ML, MC, MR, /* middle */
	BL, BC, BR; /* bottom */
};
