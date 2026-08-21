package net.leitingsd.litematicaconversion.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ConversionSelectionManager
{
    private static final ConversionSelectionManager INSTANCE =
            new ConversionSelectionManager();

    private final List<File> files =
            new ArrayList<>();

    private ConversionSelectionManager()
    {}

    public static ConversionSelectionManager getInstance()
    {
        return INSTANCE;
    }
    public synchronized boolean add(File file)
    {
        if (file == null)
        {
            return false;
        }

        File absolute =
                file.getAbsoluteFile();
        for (File existing : this.files)
        {
            if (existing.equals(absolute))
            {
                return false;
            }
        }
        this.files.add(absolute);
        return true;
    }

    public synchronized boolean remove(File file)
    {
        if (file == null)
        {
            return false;
        }

        return this.files.remove(
                file.getAbsoluteFile()
        );
    }

    public synchronized boolean contains(File file)
    {
        if (file == null)
        {
            return false;
        }

        return this.files.contains(
                file.getAbsoluteFile()
        );
    }

    public synchronized int size()
    {
        return this.files.size();
    }

    public synchronized boolean isEmpty()
    {
        return this.files.isEmpty();
    }

    public synchronized List<File> getFiles()
    {
        return Collections.unmodifiableList(
                new ArrayList<>(this.files)
        );
    }

    public synchronized void clear()
    {
        this.files.clear();
    }
}