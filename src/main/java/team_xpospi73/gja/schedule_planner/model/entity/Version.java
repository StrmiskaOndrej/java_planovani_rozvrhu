/*
 * authors:  Miroslav Pospíšil
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner.model.entity;

/**
 *  Represents schedule or exams versions
 */
public class Version {
    public int verzeId;
    public String nazev;
    public int cisloVerze;
    public boolean jeAktivniFlag;

    public int getVerzeId() {
        return verzeId;
    }

    public void setVerzeId(int verzeId) {
        this.verzeId = verzeId;
    }

    public String getNazev() {
        return nazev;
    }

    public void setNazev(String nazev) {
        this.nazev = nazev;
    }

    public int getCisloVerze() {
        return cisloVerze;
    }

    public void setCisloVerze(int cisloVerze) {
        this.cisloVerze = cisloVerze;
    }

    public boolean isJeAktivniFlag() {
        return jeAktivniFlag;
    }

    public void setJeAktivniFlag(boolean jeAktivniFlag) {
        this.jeAktivniFlag = jeAktivniFlag;
    }
}
