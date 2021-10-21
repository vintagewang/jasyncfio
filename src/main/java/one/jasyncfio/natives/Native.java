package one.jasyncfio.natives;

import java.util.Locale;

public class Native {

    static {
        try {
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            if (!os.contains("linux")) {
                throw new RuntimeException("only supported on linux");
            }
            System.load(Utils.loadLib("libjasyncfio.so").toPath().toAbsolutePath().toString());
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static int ioUringEnter(int ringFd, int toSubmit, int minComplete, int flags) {
        int ret = ioUringEnter0(ringFd, toSubmit, minComplete, flags);
        if (ret < 0) {
            throw new RuntimeException("io_uring enter error: " + ret);
        }
        return ret;
    }

    private static boolean isPowerOfTwo(int x) {
        return (x != 0) && ((x & (x - 1)) == 0);
    }

    public static Uring setupIoUring(int entries, int flags) {
        if (entries > 4096 || !isPowerOfTwo(entries)) {
            throw new IllegalArgumentException("entries must be power of 2 and less than 4096");
        }
        long[][] pointers = setupIouring0(entries, flags, 0, 0);
        final SubmissionQueue submissionQueue = new SubmissionQueue(
                pointers[0][0],
                pointers[0][1],
                pointers[0][2],
                pointers[0][3],
                pointers[0][4],
                pointers[0][5],
                pointers[0][6],
                pointers[0][7],
                (int) pointers[0][8],
                pointers[0][9],
                (int) pointers[0][10]
        );
        final CompletionQueue completionQueue = new CompletionQueue(
                pointers[1][0],
                pointers[1][1],
                pointers[1][2],
                pointers[1][3],
                pointers[1][4],
                pointers[1][5],
                pointers[1][6],
                (int) pointers[1][7],
                (int) pointers[1][8]
        );
        return new Uring(completionQueue, submissionQueue);
    }

    private static native int ioUringEnter0(int ringFd, int toSubmit, int minComplete, int flags);

    private static native long[][] setupIouring0(int entries, int flags, int sqThreadCpu, int cqEntries);

    public static native int getEventFd();
    public static native int eventFdWrite(int fd, long value);

    static native long getDirectBufferAddress(java.nio.Buffer buffer);

    public static native long getStringPointer(String str);

    public static native void releaseString(String str, long ptr);

    public static native String kernelVersion();

    /**
     * took from netty io uring project
     */
    public static boolean checkKernelVersion(String kernelVersion) {
        String[] versionComponents = kernelVersion.split("\\.");
        if (versionComponents.length < 3) {
            return false;
        }

        int major;
        try {
            major = Integer.parseInt(versionComponents[0]);
        } catch (NumberFormatException e) {
            return false;
        }

        if (major <= 4) {
            return false;
        }
        if (major > 5) {
            return true;
        }

        int minor;
        try {
            minor = Integer.parseInt(versionComponents[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        return minor >= 11;
    }

    public static final int EAGAIN = ErrnoConstants.getEagain();
    public static final int EBUSY = ErrnoConstants.getEbusy();
    public static final int EBADF = ErrnoConstants.getEbadf();
    public static final int EFAULT = ErrnoConstants.getEfault();
    public static final int EINVAL = ErrnoConstants.getEinval();
    public static final int ENXIO = ErrnoConstants.getEnxio();
    public static final int EOPNOTSUPP = ErrnoConstants.getEopnotsupp();
    public static final int EINTR = ErrnoConstants.getIntr();
    public static final int ENOENT = ErrnoConstants.getEnoent();
    public static final int EBADFD = ErrnoConstants.getEbadfd();
    public static final int ENOTDIR = ErrnoConstants.getEnotdir();
    public static final int EACCES = ErrnoConstants.getEacces();

    public static final byte IORING_OP_READ = UringConstants.ioRingOpRead();
    public static final byte IORING_OP_WRITE = UringConstants.ioRingOpWrite();
    public static final byte IORING_OP_CLOSE = UringConstants.ioRingOpClose();
    public static final byte IORING_OP_OPENAT = UringConstants.ioRingOpenAt();
    public static final byte IORING_OP_NOP = UringConstants.ioRingOpNop();
    public static final byte IORING_OP_STATX = UringConstants.ioRingOpStatx();
    public static final byte IORING_OP_FSYNC = UringConstants.ioRingOpFsync();
    public static final byte IORING_OP_FALLOCATE = UringConstants.ioRingOpFallocate();
    public static final byte IORING_OP_UNLINKAT = UringConstants.ioRingOpUnlinkAt();
    public static final byte IORING_OP_RENAMEAT = UringConstants.ioRingOpRenameAt();

    public static final int IORING_ENTER_GETEVENTS = UringConstants.ioRingEnterGetEvents();
    public static final int IORING_ENTER_SQ_WAKEUP = UringConstants.ioRingEnterSqWakeup();
    public static final int IORING_SQ_NEED_WAKEUP = UringConstants.ioRingSqNeedWakeup();
    public static final int IORING_FSYNC_DATASYNC = UringConstants.ioRingFsyncDatasync();

    public static final int O_RDONLY = FileIoConstants.oRdOnly();
    public static final int O_WRONLY = FileIoConstants.oWrOnly();
    public static final int O_RDWR = FileIoConstants.oRdWr();
    public static final int O_TRUNC = FileIoConstants.oTrunc();
    public static final int O_CREAT = FileIoConstants.oCreat();
    public static final int STATX_SIZE = FileIoConstants.statxSize();
}
