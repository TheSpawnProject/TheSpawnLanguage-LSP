package net.programmer.igoodie.lsp.capability;

import net.programmer.igoodie.goodies.registry.Registrable;
import net.programmer.igoodie.goodies.util.TypeUtilities;
import org.eclipse.lsp4j.ServerCapabilities;

public abstract class Capabilities<T> implements Registrable<Class<?>> {

    @Override
    public Class<?> getId() {
        return ((Class<?>) TypeUtilities.getSuperGenericTypes(this)[0]);
    }

    public abstract T buildOptions();

    public abstract void register(ServerCapabilities serverCapabilities, T options);

    public void register(ServerCapabilities serverCapabilities) {
        T option = this.buildOptions();
        this.register(serverCapabilities, option);
    }

}
