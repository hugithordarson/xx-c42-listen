package xxc42.data.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.UUID;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.property.BaseProperty;
import org.apache.cayenne.exp.property.EntityProperty;
import org.apache.cayenne.exp.property.ListProperty;
import org.apache.cayenne.exp.property.PropertyFactory;
import org.apache.cayenne.exp.property.StringProperty;

import xxc42.data.PersonWithAppGeneratedUUID;

/**
 * Class _PersonWithAppGeneratedUUID was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _PersonWithAppGeneratedUUID extends BaseDataObject {

    private static final long serialVersionUID = 1L;

    public static final String ID_PK_COLUMN = "id";

    public static final BaseProperty<UUID> ID = PropertyFactory.createBase("id", UUID.class);
    public static final StringProperty<String> NAME = PropertyFactory.createString("name", String.class);
    public static final ListProperty<PersonWithAppGeneratedUUID> CHILDREN = PropertyFactory.createList("children", PersonWithAppGeneratedUUID.class);
    public static final EntityProperty<PersonWithAppGeneratedUUID> LAST_ADDED_CHILD = PropertyFactory.createEntity("lastAddedChild", PersonWithAppGeneratedUUID.class);
    public static final EntityProperty<PersonWithAppGeneratedUUID> PARENT = PropertyFactory.createEntity("parent", PersonWithAppGeneratedUUID.class);

    protected UUID id;
    protected String name;

    protected Object children;
    protected Object lastAddedChild;
    protected Object parent;

    public void setId(UUID id) {
        beforePropertyWrite("id", this.id, id);
        this.id = id;
    }

    public UUID getId() {
        beforePropertyRead("id");
        return this.id;
    }

    public void setName(String name) {
        beforePropertyWrite("name", this.name, name);
        this.name = name;
    }

    public String getName() {
        beforePropertyRead("name");
        return this.name;
    }

    public void addToChildren(PersonWithAppGeneratedUUID obj) {
        addToManyTarget("children", obj, true);
    }

    public void removeFromChildren(PersonWithAppGeneratedUUID obj) {
        removeToManyTarget("children", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<PersonWithAppGeneratedUUID> getChildren() {
        return (List<PersonWithAppGeneratedUUID>)readProperty("children");
    }

    public void setLastAddedChild(PersonWithAppGeneratedUUID lastAddedChild) {
        setToOneTarget("lastAddedChild", lastAddedChild, true);
    }

    public PersonWithAppGeneratedUUID getLastAddedChild() {
        return (PersonWithAppGeneratedUUID)readProperty("lastAddedChild");
    }

    public void setParent(PersonWithAppGeneratedUUID parent) {
        setToOneTarget("parent", parent, true);
    }

    public PersonWithAppGeneratedUUID getParent() {
        return (PersonWithAppGeneratedUUID)readProperty("parent");
    }

    protected abstract void onPostAdd();

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "id":
                return this.id;
            case "name":
                return this.name;
            case "children":
                return this.children;
            case "lastAddedChild":
                return this.lastAddedChild;
            case "parent":
                return this.parent;
            default:
                return super.readPropertyDirectly(propName);
        }
    }

    @Override
    public void writePropertyDirectly(String propName, Object val) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch (propName) {
            case "id":
                this.id = (UUID)val;
                break;
            case "name":
                this.name = (String)val;
                break;
            case "children":
                this.children = val;
                break;
            case "lastAddedChild":
                this.lastAddedChild = val;
                break;
            case "parent":
                this.parent = val;
                break;
            default:
                super.writePropertyDirectly(propName, val);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        writeSerialized(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        readSerialized(in);
    }

    @Override
    protected void writeState(ObjectOutputStream out) throws IOException {
        super.writeState(out);
        out.writeObject(this.id);
        out.writeObject(this.name);
        out.writeObject(this.children);
        out.writeObject(this.lastAddedChild);
        out.writeObject(this.parent);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.id = (UUID)in.readObject();
        this.name = (String)in.readObject();
        this.children = in.readObject();
        this.lastAddedChild = in.readObject();
        this.parent = in.readObject();
    }

}
