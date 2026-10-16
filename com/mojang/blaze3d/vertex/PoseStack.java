package com.mojang.blaze3d.vertex;

import com.google.common.collect.Queues;
import com.mojang.math.MatrixUtil;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class PoseStack implements net.neoforged.neoforge.client.extensions.IPoseStackExtension {
    private final Deque<PoseStack.Pose> poseStack = Util.make(Queues.newArrayDeque(), p_85848_ -> {
        Matrix4f matrix4f = new Matrix4f();
        Matrix3f matrix3f = new Matrix3f();
        p_85848_.add(new PoseStack.Pose(matrix4f, matrix3f));
    });

    public void translate(double x, double y, double z) {
        this.translate((float)x, (float)y, (float)z);
    }

    public void translate(float x, float y, float z) {
        PoseStack.Pose posestack$pose = this.poseStack.getLast();
        posestack$pose.pose.translate(x, y, z);
    }

    public void scale(float x, float y, float z) {
        PoseStack.Pose posestack$pose = this.poseStack.getLast();
        posestack$pose.pose.scale(x, y, z);
        if (Math.abs(x) == Math.abs(y) && Math.abs(y) == Math.abs(z)) {
            if (x < 0.0F || y < 0.0F || z < 0.0F) {
                posestack$pose.normal.scale(Math.signum(x), Math.signum(y), Math.signum(z));
            }
        } else {
            posestack$pose.normal.scale(1.0F / x, 1.0F / y, 1.0F / z);
            posestack$pose.trustedNormals = false;
        }
    }

    public void mulPose(Quaternionf p_254385_) {
        PoseStack.Pose posestack$pose = this.poseStack.getLast();
        posestack$pose.pose.rotate(p_254385_);
        posestack$pose.normal.rotate(p_254385_);
    }

    public void rotateAround(Quaternionf p_272904_, float p_273581_, float p_272655_, float p_273275_) {
        PoseStack.Pose posestack$pose = this.poseStack.getLast();
        posestack$pose.pose.rotateAround(p_272904_, p_273581_, p_272655_, p_273275_);
        posestack$pose.normal.rotate(p_272904_);
    }

    public void pushPose() {
        this.poseStack.addLast(new PoseStack.Pose(this.poseStack.getLast()));
    }

    public void popPose() {
        this.poseStack.removeLast();
    }

    public PoseStack.Pose last() {
        return this.poseStack.getLast();
    }

    public boolean clear() {
        return this.poseStack.size() == 1;
    }

    public void setIdentity() {
        PoseStack.Pose posestack$pose = this.poseStack.getLast();
        posestack$pose.pose.identity();
        posestack$pose.normal.identity();
        posestack$pose.trustedNormals = true;
    }

    public void mulPose(Matrix4f p_324519_) {
        PoseStack.Pose posestack$pose = this.poseStack.getLast();
        posestack$pose.pose.mul(p_324519_);
        if (!MatrixUtil.isPureTranslation(p_324519_)) {
            if (MatrixUtil.isOrthonormal(p_324519_)) {
                posestack$pose.normal.mul(new Matrix3f(p_324519_));
            } else {
                posestack$pose.computeNormalMatrix();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Pose {
        final Matrix4f pose;
        final Matrix3f normal;
        boolean trustedNormals = true;

        Pose(Matrix4f p_254509_, Matrix3f p_254348_) {
            this.pose = p_254509_;
            this.normal = p_254348_;
        }

        Pose(PoseStack.Pose p_324130_) {
            this.pose = new Matrix4f(p_324130_.pose);
            this.normal = new Matrix3f(p_324130_.normal);
            this.trustedNormals = p_324130_.trustedNormals;
        }

        void computeNormalMatrix() {
            this.normal.set(this.pose).invert().transpose();
            this.trustedNormals = false;
        }

        public Matrix4f pose() {
            return this.pose;
        }

        public Matrix3f normal() {
            return this.normal;
        }

        public Vector3f transformNormal(Vector3f p_324099_, Vector3f p_323689_) {
            return this.transformNormal(p_324099_.x, p_324099_.y, p_324099_.z, p_323689_);
        }

        public Vector3f transformNormal(float x, float y, float z, Vector3f destination) {
            Vector3f vector3f = this.normal.transform(x, y, z, destination);
            return this.trustedNormals ? vector3f : vector3f.normalize();
        }

        public PoseStack.Pose copy() {
            return new PoseStack.Pose(this);
        }
    }
}
