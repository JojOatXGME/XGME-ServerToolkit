package de.xgme.mc.toolkit.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class ArgumentAnalyzer {

	private boolean analyzed = false;
	private HashMap<String, Option> options = new HashMap<String, Option>();
	private ArrayList<String> arguments = new ArrayList<String>();

	public void addOption(String name, int arguments) {
		this.options.put(name, new Option(arguments));
	}

	public void addAlias(String alias, String opt) {
		final Option option = this.options.get(opt);
		if (option == null)
			throw new NoSuchElementException("Option not found: " + opt);

		this.options.put(alias, option);
	}

	public boolean analyze(final String... args) {
		if (analyzed)
			throw new IllegalStateException("Command already analyzed.");
		analyzed = true;

		for (int i = 0; i < args.length; ++i) {
			if (args[i].startsWith("-")) {
				final Option option = this.options.get(args[i].substring(1));
				if (option == null)
					return false;

				option.inCommand = true;
				option.args = new String[option.argsCount];
				for (int j = 0; j < option.argsCount; ++j) {
					i += 1;
					if (i >= args.length)
						return false;

					option.args[j] = args[i];
				}
			} else {
				this.arguments.add(args[i]);
			}
		}

		return true;
	}

	public int getArgumentCount() {
		return this.arguments.size();
	}

	public String[] getArguments() {
		return this.arguments.toArray(new String[this.arguments.size()]);
	}

	public String getArgument(int index) {
		return this.arguments.get(index);
	}

	public Option getOption(String option) {
		return this.options.get(option);
	}

	public static class Option {
		private final int argsCount;

		private boolean inCommand = false;
		private String[] args = null;

		private Option(int arguments) {
			this.argsCount = arguments;
		}

		public boolean isInCommand() {
			return inCommand;
		}

		public String[] getArguments() {
			return args;
		}

		public String getArgument(int index) {
			return args[index];
		}
	}
}
