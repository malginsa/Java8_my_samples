package java8_my_samples.lesson160510;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class WordCounterParallel {

	public static final String DANTE =
            " Nel   mezzo del cammin  di nostra  vita " +
            "mi  ritrovai in una  selva oscura" +
            " che la  dritta via era   smarrita ";

	public static final String ONEGIN =
			"Не мысля гордый свет забавить, \n"
			+ "Вниманье дружбы возлюбя, \n"
			+ "Хотел бы я тебе представить \n"
			+ "Залог достойнее тебя, \n"
			+ "Достойнее души прекрасной, \n"
			+ "Святой исполненной мечты, \n"
			+ "Поэзии живой и ясной, \n"
			+ "Высоких дум и простоты; \n"
			+ "Но так и быть - рукой пристрастной \n"
			+ "Прими собранье пестрых глав, \n"
			+ "Полусмешных, полупечальных, \n"
			+ "Простонародных, идеальных, \n"
			+ "Небрежный плод моих забав, \n"
			+ "Бессонниц, легких вдохновений, \n"
			+ "Незрелых и увядших лет, \n"
			+ "Ума холодных наблюдений \n"
			+ "И сердца горестных замет.\n";

	public static String SENTENCE = ONEGIN;
	
	private static class WordCounter {
		private final int counter;
		private final boolean lastSpace;

		public WordCounter(int counter, boolean lastSpace) {
			this.counter = counter;
			this.lastSpace = lastSpace;
		}

		public WordCounter accumulate(Character c) {
			if (Character.isWhitespace(c)) {
				return lastSpace ? this : new WordCounter(counter, true);
			} else {
				return lastSpace ? new WordCounter(counter + 1, false) : this;
			}
		}

		public WordCounter combine(WordCounter wordCounter) {
			return new WordCounter(counter + wordCounter.counter, wordCounter.lastSpace);
		}

		public int getCounter() {
			return counter;
		}
	}

	private static class WordCounterSpliterator implements Spliterator<Character> {

		private final String string;
		private int currentChar = 0;

		private WordCounterSpliterator(String string) {
			this.string = string;
		}

		@Override
		public boolean tryAdvance(Consumer<? super Character> action) {
			action.accept(string.charAt(currentChar++));
			return currentChar < string.length();
		}

		@Override
		public Spliterator<Character> trySplit() {
			int currentSize = string.length() - currentChar;
			if (currentSize < 10) {
				return null;
			}
			for (int splitPos = currentSize / 2 + currentChar; 
					splitPos < string.length(); 
					splitPos++) {
				if (Character.isWhitespace(string.charAt(splitPos))) {
					Spliterator<Character> spliterator = new WordCounterSpliterator(
							string.substring(currentChar, splitPos));
					currentChar = splitPos;
					return spliterator;
				}
			}
			return null;
		}

		@Override
		public long estimateSize() {
			return string.length() - currentChar;
		}

		@Override
		public int characteristics() {
			return ORDERED + SIZED + SUBSIZED + NONNULL + IMMUTABLE;
		}
	}	
	
	public static int countWordsIteratively(String s) {
		int counter = 0;
		boolean lastSpace = true;
		for (char c : s.toCharArray()) {
			if (Character.isWhitespace(c)) {
				lastSpace = true;
			} else {
				if (lastSpace) {
					counter++;
				}
				lastSpace = Character.isWhitespace(c);
			}
		}
		return counter;
	}

	private static int countWordsInParallel(String sentence) {

		WordCounterSpliterator spliterator = new WordCounterSpliterator(sentence);
		Stream<Character> streamSplittered = StreamSupport.stream(spliterator, true);

		WordCounter wordCounter = streamSplittered.reduce(new WordCounter(0, true), 
				WordCounter::accumulate, WordCounter::combine);
		return wordCounter.getCounter();
	}

	public static void main(String[] args) {

//		SENTENCE = DANTE;
		System.out.println("Found " + countWordsIteratively(SENTENCE) + " words");
		System.out.println("Found " + countWordsInParallel(SENTENCE) + " words");

	}

}
