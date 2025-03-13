package xxc42.data.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.property.EntityProperty;
import org.apache.cayenne.exp.property.ListProperty;
import org.apache.cayenne.exp.property.PropertyFactory;
import org.apache.cayenne.exp.property.StringProperty;

import xxc42.data.Company;
import xxc42.data.Person;

/**
 * Class _Division was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Division extends BaseDataObject {

    private static final long serialVersionUID = 1L;

    public static final String ID_PK_COLUMN = "id";

    public static final StringProperty<String> NAME = PropertyFactory.createString("name", String.class);
    public static final EntityProperty<Company> COMPANY = PropertyFactory.createEntity("company", Company.class);
    public static final ListProperty<Person> EMPLOYEES = PropertyFactory.createList("employees", Person.class);
    public static final EntityProperty<Person> MANAGER = PropertyFactory.createEntity("manager", Person.class);

    protected String name;

    protected Object company;
    protected Object employees;
    protected Object manager;

    public void setName(String name) {
        beforePropertyWrite("name", this.name, name);
        this.name = name;
    }

    public String getName() {
        beforePropertyRead("name");
        return this.name;
    }

    public void setCompany(Company company) {
        setToOneTarget("company", company, true);
    }

    public Company getCompany() {
        return (Company)readProperty("company");
    }

    public void addToEmployees(Person obj) {
        addToManyTarget("employees", obj, true);
    }

    public void removeFromEmployees(Person obj) {
        removeToManyTarget("employees", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<Person> getEmployees() {
        return (List<Person>)readProperty("employees");
    }

    public void setManager(Person manager) {
        setToOneTarget("manager", manager, true);
    }

    public Person getManager() {
        return (Person)readProperty("manager");
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "name":
                return this.name;
            case "company":
                return this.company;
            case "employees":
                return this.employees;
            case "manager":
                return this.manager;
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
            case "name":
                this.name = (String)val;
                break;
            case "company":
                this.company = val;
                break;
            case "employees":
                this.employees = val;
                break;
            case "manager":
                this.manager = val;
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
        out.writeObject(this.name);
        out.writeObject(this.company);
        out.writeObject(this.employees);
        out.writeObject(this.manager);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.name = (String)in.readObject();
        this.company = in.readObject();
        this.employees = in.readObject();
        this.manager = in.readObject();
    }

}
