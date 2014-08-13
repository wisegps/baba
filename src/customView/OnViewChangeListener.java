package customView;

public interface OnViewChangeListener {
	public abstract void OnViewChange(int view);
	public abstract void OnLastView();
	public void OnFinish(int index);
}
