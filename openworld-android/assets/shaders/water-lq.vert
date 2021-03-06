attribute vec3 a_position;

uniform mat4 u_projModelView;

varying vec4 v_position;

void main()
{
	v_position = vec4(a_position, 1.0);
	gl_Position =  u_projModelView * v_position;
}
