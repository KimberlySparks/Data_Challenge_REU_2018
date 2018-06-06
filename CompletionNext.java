/**
 * Copyright 2016 University of Zurich
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package examples;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import cc.kave.commons.model.events.CommandEvent;
import cc.kave.commons.model.events.IDEEvent;
import cc.kave.commons.model.events.NavigationEvent;
import cc.kave.commons.model.events.completionevents.CompletionEvent;
import cc.kave.commons.model.events.visualstudio.EditEvent;
import cc.kave.commons.model.ssts.ISST;
import cc.kave.commons.utils.io.IReadingArchive;
import cc.kave.commons.utils.io.ReadingArchive;

public class CompletionNext {
	// NOTE: Some edits were made to other files, such as IDEEvent to create getters for some
	// values that could not be accessed when we first downloaded the Java.
	
	// Variable declarations
	private FileWriter fw;
	private PrintWriter pw;
	private String eventsDir;

	/** Constructor that reads in the Events folder of data */
	public CompletionNext(String eventsDir) {
		this.eventsDir = eventsDir;
	}
	/** Main */
	public void run() {
		createCSV();
		System.out.printf("looking (recursively) for events in folder %s\n", new File(eventsDir).getAbsolutePath());
		Set<String> userZips = IoHelper.findAllZips(eventsDir);
		for (String userZip : userZips) {
			System.out.printf("\n##### processing user zip: %s #####\n", userZip);
			processUserZip(userZip);
		}
		closeCSV();
	}
	/** Processes each individual zip */
	private void processUserZip(String userZip) {
		int numProcessedEvents = 0;
		try (IReadingArchive ra = new ReadingArchive(new File(eventsDir, userZip))) {
			while (ra.hasNext() && (numProcessedEvents++ < 1000)) {
				IDEEvent e = ra.getNext(IDEEvent.class);
				//processEvent(e);
				//if (e instanceof CompletionEvent) {
				//	process((CompletionEvent) e);
				//	e = ra.getNext(IDEEvent.class);
				//	//if (e instanceof !Completion)
				//	processBasic(e);
				//}
				processSimple(e);
			}
		}
	}
	/** Determines how to process each type of event */ 
	private void processEvent(IDEEvent e) {
		if (e instanceof CommandEvent) {
			process((CommandEvent) e);
		} else if (e instanceof CompletionEvent) {
			process((CompletionEvent) e);
		} else if (e instanceof EditEvent) {
			process((EditEvent) e);
		} else if (e instanceof NavigationEvent) {
			process((NavigationEvent) e);
		} else{
			processBasic(e);
		}
	}
	/** Creates format to process CommandEvent, including special fields */
	private void process(CommandEvent e) {
		Duration duration = e.getDuration();
		duration.toMillis();
		System.out.printf("CommandEvent\n");
		pw.printf("CommandEvent,");
		pw.printf("%s,", DateTimeFormatter.ofPattern("MM/dd/yyyy,hh:mm:ss aa").format(e.getTriggeredAt()));
		pw.printf("%s,", e.getTriggeredBy());
		try {
			pw.printf("%s,", DateTimeFormatter.ofPattern("hh:mm:ss aa").format(e.getTerminatedAt()));
		} catch(NullPointerException n) {
			pw.printf("null,");
		}
		pw.printf("%s,", duration);
		pw.printf("%s,\n", e.getCommandId());
	}
	/** Creates format to process CompletionEvent, including special fields */
	private void process(CompletionEvent e) {
		Duration duration = e.getDuration();
		duration.toMillis();
		System.out.printf("CompletionEvent\n");
		pw.printf("CompletionEvent,");
		pw.printf("%s,", DateTimeFormatter.ofPattern("MM/dd/yyyy,hh:mm:ss aa").format(e.getTriggeredAt()));
		pw.printf("%s,", e.getTriggeredBy());
		pw.printf("%s,", DateTimeFormatter.ofPattern("hh:mm:ss aa").format(e.getTerminatedAt()));
		pw.printf("%s,", duration);
		pw.printf("%s,", e.getTerminatedBy());
		pw.printf("%s,", e.getTerminatedState());
		pw.printf("%s,\n", e.getProposalCount());
	}
	/** Creates format to process EditEvent, including special fields */
	private void process(EditEvent e) {
		Duration duration = e.getDuration();
		duration.toMillis();
		System.out.printf("EditEvent\n");
		pw.printf("EditEvent,");
		pw.printf("%s,", DateTimeFormatter.ofPattern("MM/dd/yyyy,hh:mm:ss aa").format(e.getTriggeredAt()));
		pw.printf("%s,", e.getTriggeredBy());
		pw.printf("%s,", DateTimeFormatter.ofPattern("hh:mm:ss aa").format(e.getTerminatedAt()));
		pw.printf("%s,", duration);
		pw.printf("%s,", e.NumberOfChanges);
		pw.printf("%s\n", e.SizeOfChanges);
	}
	/** Creates format to process NavigationEvent, including special fields */
	private void process(NavigationEvent e) {
		Duration duration = e.getDuration();
		duration.toMillis();
		System.out.printf("NavigationEvent\n");
		pw.printf("NavigationEvent,");
		pw.printf("%s,", DateTimeFormatter.ofPattern("MM/dd/yyyy,hh:mm:ss aa").format(e.getTriggeredAt()));
		pw.printf("%s,", e.getTriggeredBy());
		try {
			pw.printf("%s,", DateTimeFormatter.ofPattern("hh:mm:ss aa").format(e.getTerminatedAt()));
		} catch(NullPointerException n) {
			pw.printf("null,");
		}
		pw.printf("%s,", duration);
		pw.printf("%s\n", e.TypeOfNavigation);
	}
	/** Creates format to process any event type, covering universal fields */
	private void processBasic(IDEEvent e) {
		String eventType = e.getClass().getSimpleName();
		System.out.printf("%s\n", eventType);
		pw.printf("%s,", eventType);
		pw.printf("%s,", DateTimeFormatter.ofPattern("MM/dd/yyyy,hh:mm:ss aa").format(e.getTriggeredAt()));
		pw.printf("%s,", e.getTriggeredBy());
		try {
			pw.printf("%s,", DateTimeFormatter.ofPattern("hh:mm:ss aa").format(e.getTerminatedAt()));
			Duration duration = e.getDuration();
			duration.toMillis();
			pw.printf("%s\n", duration);
		} catch (NullPointerException n) {
			pw.printf("null");
		}
	}
	/** A variation on processBasic that only includes specific pertinent data */
	private void processSimple(IDEEvent e) {
		String eventType = e.getClass().getSimpleName();
		pw.printf("%s,", eventType);
		pw.printf("%s,", e.IDESessionUUID);
		pw.printf("%s\n", DateTimeFormatter.ofPattern("MM/dd/yyyy,hh:mm:ss a").format(e.getTriggeredAt()));
	}
	/** Creates the CSV file */
	private void createCSV() {
		try {
			// File path for the CSV file
			fw = new FileWriter("C://Users//kspar//Documents//Data Challenge//getting_started_simplified.csv");
			pw = new PrintWriter(fw);
		} catch (IOException i) {
			i.printStackTrace();
		}
	}
	/** Closes declarations for file and print writers */
	private void closeCSV() {
		try {
			fw.close();
			pw.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}
}
