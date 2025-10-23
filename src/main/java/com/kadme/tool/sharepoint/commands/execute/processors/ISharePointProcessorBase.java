/* Decompiler 5ms, total 231ms, lines 14 */
package com.kadme.tool.sharepoint.commands.execute.processors;

import com.kadme.rest.exception.WhereoilException;
import java.io.Closeable;

public interface ISharePointProcessorBase extends Closeable {
   String FILE_ITEMS_CLASS_NAME = "Documents";
   String LISTITEMS_ITEMS_CLASS_NAME = "ListItems";

   boolean execute() throws WhereoilException;

   String getStatus();
}
