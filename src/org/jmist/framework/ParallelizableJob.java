/**
 *
 */
package org.jmist.framework;

import org.jmist.framework.reporting.ProgressMonitor;

/**
 * Represents a job that can be split into smaller chunks.
 * @author bkimmel
 */
public interface ParallelizableJob extends Job {

	/**
	 * Gets the next task to be performed.
	 * @return The <code>Object</code> describing the next task to be
	 * 		performed, or <code>null</code> if there are no remaining
	 * 		tasks.
	 */
	Object getNextTask();

	/**
	 * Submits the results of a task.
	 * @param task The <code>Object</code> describing the task for which
	 * 		results are being submitted (must have been obtained from a
	 * 		previous call to {@link #getNextTask()}.
	 * @param results The <code>Object</code> containing the results of
	 * 		a task.
	 * @param monitor The <code>ProgressMonitor</code> to update with the
	 * 		progress of this <code>Job</code>.
	 * @see {@link #getNextTask()}.
	 */
	void submitTaskResults(Object task, Object results, ProgressMonitor monitor);

	/**
	 * Gets the task worker to use to process the tasks of this
	 * job.
	 * @return The task worker to use to process the tasks of
	 * 		this job.
	 */
	TaskWorker worker();

}