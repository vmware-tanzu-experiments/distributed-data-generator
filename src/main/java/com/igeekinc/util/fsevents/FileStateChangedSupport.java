/*
 * Copyright 2002-2014 iGeek, Inc.
 * All Rights Reserved
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igeekinc.util.fsevents;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;

import com.igeekinc.util.CheckCorrectDispatchThread;
import com.igeekinc.util.DeferredEventProcessor;
import com.igeekinc.util.FilePath;
import com.igeekinc.util.SystemInfo;
import com.igeekinc.util.Volume;
import com.igeekinc.util.VolumeManager;

public class FileStateChangedSupport extends DeferredEventProcessor
{
	private HashMap<FilePath, ArrayList<WeakReference<FileStateChangedEventListener>>>listeners = new HashMap<FilePath, ArrayList<WeakReference<FileStateChangedEventListener>>>();
	
	public FileStateChangedSupport(CheckCorrectDispatchThread checkDispatcher)
	{
		super(checkDispatcher);
		SystemInfo.getSystemInfo().getVolumeManager().addPropertyChangeListener(VolumeManager.kVolumeAddedPropertyName, new PropertyChangeListener()
		{
			
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				volumeAdded((Volume)evt.getNewValue());
			}
		});
		SystemInfo.getSystemInfo().getVolumeManager().addPropertyChangeListener(VolumeManager.kVolumeRemovedPropertyName, new PropertyChangeListener()
		{
			
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				volumeRemoved((Volume)evt.getNewValue());
			}
		});
	}

	protected void volumeRemoved(Volume removedVolume)
	{
		notifyAllForVolume(removedVolume);
	}

	protected void volumeAdded(Volume addedVolume)
	{
		notifyAllForVolume(addedVolume);
	}
	
	protected void notifyAllForVolume(Volume notifyVolume)
	{
		ArrayList<FileStateChangedEvent>fireEvents = new ArrayList<FileStateChangedEvent>();
		synchronized(listeners)
		{
			for (FilePath curCheckPath:listeners.keySet())
			{
				if (curCheckPath.startsWith(notifyVolume.getRoot().getFilePath()))
				{
					FileStateChangedEvent fireEvent = new FileStateChangedEvent(this, curCheckPath);
					fireEvents.add(fireEvent);
				}
			}
		}
		for (FileStateChangedEvent eventToFire:fireEvents)
		{
			fireEventOnCorrectThread(eventToFire);
		}
	}

	/*
	 * Walks through a list of weak references and creates a new list with the actual objects and removes any vacated references from
	 * the original list
	 */
	private ArrayList<FileStateChangedEventListener> checkAndGetListeners(ArrayList<WeakReference<FileStateChangedEventListener>> notifyReferences)
	{
		ArrayList<FileStateChangedEventListener> returnList = new ArrayList<FileStateChangedEventListener>();
		Iterator<WeakReference<FileStateChangedEventListener>> refIterator = notifyReferences.iterator();
		while(refIterator.hasNext())
		{
			WeakReference<FileStateChangedEventListener>curRef = refIterator.next();
			FileStateChangedEventListener curListener = curRef.get();
			if (curListener != null)
			{
				returnList.add(curListener);
			}
			else
			{
				refIterator.remove();	// It's dead Jim!
			}
		}
		return returnList;
	}

	@Override
	public void fireEvent(EventObject eventToFire)
	{
		FileStateChangedEvent fscEventToFire = (FileStateChangedEvent)eventToFire;
		FilePath changedPath = fscEventToFire.getChangedPath();
		ArrayList<FileStateChangedEventListener>notifyListeners;
		synchronized(listeners)
		{
			ArrayList<WeakReference<FileStateChangedEventListener>>notifyReferences = listeners.get(changedPath);
			notifyListeners = checkAndGetListeners(notifyReferences);
			if (notifyListeners.size() == 0)	// Nobody alive listening
			{
				listeners.remove(changedPath);
			}
		}
		for (FileStateChangedEventListener curListener:notifyListeners)
		{
			curListener.fileStateChanged(fscEventToFire);
		}
	}

	@Override
	public void fireVetoableEvent(EventObject eventToFire) throws PropertyVetoException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void notifyFiringThread()
	{
		dispatcher.fireEventsOnDispatchThread(this);
	}
	
	public void addFileStateChangeListener(FilePath listenPath, FileStateChangedEventListener listener)
	{
		synchronized(listeners)
		{
			WeakReference<FileStateChangedEventListener>listenerRef = new WeakReference<FileStateChangedEventListener>(listener);
			ArrayList<WeakReference<FileStateChangedEventListener>>addList = listeners.get(listenPath);
			if (addList == null)
			{
				addList = new ArrayList<WeakReference<FileStateChangedEventListener>>();
				listeners.put(listenPath, addList);
			}
			addList.add(listenerRef);
		}
	}
	
	public void removeFileStateChangeListener(FilePath listenPath, FileStateChangedEventListener listener)
	{
		synchronized(listeners)
		{
			WeakReference<FileStateChangedEventListener>listenerRef = new WeakReference<FileStateChangedEventListener>(listener);
			ArrayList<WeakReference<FileStateChangedEventListener>>removeList = listeners.get(listenPath);
			if (removeList != null)
				removeList.remove(listenerRef);
		}
	}
}
